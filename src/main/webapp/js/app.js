
const api = 'resources/empleados';

const $ = selector => document.querySelector(selector);

const option = (value, label) =>
    `<option value="${escapeHtml(value)}">${escapeHtml(label)}</option>`;

const escapeHtml = value =>
    String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[char]);

let employees = [];
let catalogs = {
    departamentos: [],
    puestos: [],
    sexos: [],
    estatuses: [],
    estados: []
};

let admin = {};
let editingId = null;
let currentPage = 1;
const itemsPerPage = 5;

let employeeSearchApplied = false;
let employeeFilters = {
    numero: '',
    nombre: '',
    estatus: ''
};

const fullName = employee =>
    [employee.nombre, employee.apellidoPaterno, employee.apellidoMaterno]
        .filter(Boolean).join(' ');

const employeeLabel = employee =>
    `${fullName(employee)} (#${employee.idEmpleado})`;

const formData = form =>
    Object.fromEntries(new FormData(form));

const request = (url, method = 'GET', body) =>
    fetch(url, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : {},
        body: body ? JSON.stringify(body) : undefined
    });

const statusOptions = list =>
    list.map(item =>
        option(item.idEstatus, `${item.idEstatus} - ${item.descripcion}`)
    ).join('');

// ======================================================
// ESTADO DE LOS FORMULARIOS
// ======================================================

function setStatusFieldState(form, visible) {
    const field = form?.querySelector('[data-status-field]');
    if (!field) return;

    const select = field.querySelector('select');
    if (!select) return;

    field.style.display = visible ? '' : 'none';
    select.disabled = !visible;
    select.required = visible;

    if (!visible) select.value = 'ACTIVO';
}

function resetForm(form) {
    if (!form) return;

    form.reset();

    if (form.elements.editingId) {
        form.elements.editingId.value = '';
    }

    if (form.elements.idSexo && catalogs.sexos?.length) {
        form.elements.idSexo.value = catalogs.sexos[0].idSexo;
    }

    setStatusFieldState(form, false);

    if (form.elements.idEstatus) {
        form.elements.idEstatus.value = 'ACTIVO';
    }

    if (form.querySelector('#paises-domicilio')) {
        updateStatesOptions('');
    }
}

function editForm(form, item, id) {
    if (!form || !item) return;

    form.reset();

    if (form.elements.editingId) {
        form.elements.editingId.value = id;
    }

    Object.keys(item).forEach(key => {
        if (form.elements[key]) {
            form.elements[key].value = item[key] ?? '';
        }
    });

    if (item.pais && form.elements.idPais) {
        form.elements.idPais.value = item.pais.idPais;
    }

    fillGeneralStatuses();

    if (form.elements.idEstatus) {
        form.elements.idEstatus.value = item.idEstatus;
        setStatusFieldState(form, true);
    }
}

// ======================================================
// CARGA DE EMPLEADOS
// ======================================================

async function load() {
    try {
        const responses = await Promise.all([
            request(api),
            request(`${api}/catalogos`)
        ]);

        if (responses.some(response => !response.ok)) {
            throw Error('No se pudieron cargar los empleados.');
        }

        employees = await responses[0].json();
        catalogs = await responses[1].json();

        fillEmployeeCatalogs();
        fillFilterDropdowns();
        renderEmployees();

        await loadAdmin();

    } catch (error) {
        console.error(error);

        const table = $('#employees');

        if (table) {
            table.innerHTML = `
                <tr>
                    <td class="error" colspan="5">
                        ${escapeHtml(error.message)}
                    </td>
                </tr>
            `;
        }
    }
}

// ======================================================
// FILTROS DE EMPLEADOS
// ======================================================

function fillFilterDropdowns() {
    const select = $('#filter-estatus');
    if (!select) return;

    select.innerHTML =
        option('', 'Todos los estatus') +
        (catalogs.estatuses || []).map(item =>
            option(item.idEstatus, item.descripcion || item.idEstatus)
        ).join('');
}

function searchEmployees() {
    const numero = ($('#filter-numero')?.value || '').trim();
    const nombre = ($('#employee-search')?.value || '')
        .trim().toLowerCase();
    const estatus = $('#filter-estatus')?.value || '';

    if (!numero && !nombre && !estatus) {
        alert('Captura al menos un filtro para buscar empleados.');
        return;
    }

    employeeFilters = { numero, nombre, estatus };
    employeeSearchApplied = true;
    currentPage = 1;

    renderEmployees();
}

function clearEmployeeFilters() {
    if ($('#filter-numero')) {
        $('#filter-numero').value = '';
    }

    if ($('#employee-search')) {
        $('#employee-search').value = '';
    }

    if ($('#filter-estatus')) {
        $('#filter-estatus').value = '';
    }

    employeeFilters = {
        numero: '',
        nombre: '',
        estatus: ''
    };

    employeeSearchApplied = false;
    currentPage = 1;

    renderEmployees();
}

// ======================================================
// TABLA Y PAGINACION DE EMPLEADOS
// ======================================================

function renderEmployees() {
    const table = $('#employees');
    if (!table) return;

    if (!employeeSearchApplied) {
        table.innerHTML = `
            <tr>
                <td class="empty" colspan="5">
                    Captura un filtro y pulsa Buscar para ver empleados.
                </td>
            </tr>
        `;

        if ($('#count')) {
            $('#count').textContent = 'Consulta de personal registrado';
        }

        renderPaginationControls(0, 1);
        return;
    }

    const { numero, nombre, estatus } = employeeFilters;

    let results = [...employees].sort(
        (a, b) => Number(b.idEmpleado || 0) -
                  Number(a.idEmpleado || 0)
    );

    if (numero) {
        results = results.filter(employee =>
            String(employee.idEmpleado ?? '').includes(numero)
        );
    }

    if (nombre) {
        results = results.filter(employee =>
            fullName(employee).toLowerCase().includes(nombre)
        );
    }

    if (estatus) {
        results = results.filter(employee =>
            String(employee.estatus?.idEstatus || '') === estatus
        );
    }

    const total = results.length;
    const totalPages = Math.max(1, Math.ceil(total / itemsPerPage));

    currentPage = Math.max(1, Math.min(currentPage, totalPages));

    const start = (currentPage - 1) * itemsPerPage;
    const page = results.slice(start, start + itemsPerPage);

    if ($('#count')) {
        $('#count').textContent = `${total} empleado(s) encontrado(s)`;
    }

    table.innerHTML = page.length
        ? page.map(employee => `
            <tr>
                <td>${escapeHtml(employee.idEmpleado)}</td>

                <td>
                    <div class="name">
                        ${escapeHtml(fullName(employee))}
                    </div>
                </td>

                <td>
                    ${escapeHtml(
                        employee.correoInstitucional || 'Sin correo'
                    )}
                </td>

                <td>
                    <span class="status ${escapeHtml(
                        employee.estatus?.idEstatus || ''
                    )}">
                        ${escapeHtml(
                            employee.estatus?.idEstatus || 'Sin estatus'
                        )}
                    </span>
                </td>

                <td class="actions">
                    <button
                        type="button"
                        data-detail="${escapeHtml(employee.idEmpleado)}">
                        Ver detalle
                    </button>

                    <button
                        type="button"
                        data-edit="${escapeHtml(employee.idEmpleado)}">
                        Editar
                    </button>

                    <button
                        type="button"
                        class="danger"
                        data-delete="${escapeHtml(employee.idEmpleado)}">
                        Dar de baja
                    </button>
                </td>
            </tr>
        `).join('')
        : `
            <tr>
                <td class="empty" colspan="5">
                    No hay empleados que coincidan con los filtros.
                </td>
            </tr>
        `;

    renderPaginationControls(total, totalPages);
}

function renderPaginationControls(total, totalPages) {
    const info = $('#pagination-info');
    const label = $('#current-page');
    const prev = $('#prev-page');
    const next = $('#next-page');

    if (info) {
        info.textContent = !employeeSearchApplied
            ? 'Sin resultados'
            : total
                ? `Mostrando ${(currentPage - 1) * itemsPerPage + 1}-${Math.min(currentPage * itemsPerPage, total)} de ${total}`
                : 'Sin resultados';
    }

    if (label) {
        label.textContent = `Página ${currentPage} de ${totalPages}`;
    }

    if (prev) {
        prev.disabled = !employeeSearchApplied || currentPage <= 1;
    }

    if (next) {
        next.disabled = !employeeSearchApplied ||
                        currentPage >= totalPages;
    }
}

// ======================================================
// CATALOGOS DEL FORMULARIO DE EMPLEADOS
// ======================================================

function updateStatesOptions(idPais) {
    const select = $('#estados-domicilio');
    if (!select) return;

    if (!idPais) {
        select.innerHTML = option(
            '', 'Seleccione primero un país...'
        );
        select.disabled = true;
        return;
    }

    const states = (catalogs.estados || []).filter(
        item => String(item.pais?.idPais) === String(idPais)
    );

    select.innerHTML = states.length
        ? option('', 'Seleccione un estado...') +
          states.map(item => option(item.idEstado, item.nombre)).join('')
        : option('', 'No hay estados disponibles');

    select.disabled = states.length === 0;
}

function fillEmployeeCatalogs() {
    const fields = [
        ['#sexos', catalogs.sexos, 'idSexo', 'descripcion'],
        ['#departamentos', catalogs.departamentos, 'idDepto', 'descripcion'],
        ['#puestos', catalogs.puestos, 'idPuesto', 'descripcionPuesto']
    ];

    fields.forEach(([selector, list, id, label]) => {
        const element = $(selector);

        if (element) {
            element.innerHTML = (list || []).map(
                item => option(item[id], item[label])
            ).join('');
        }
    });

    const statuses = $('#estatuses');

    if (statuses) {
        statuses.innerHTML = statusOptions(catalogs.estatuses || []);
    }

    const countries = admin.countries?.length
        ? admin.countries
        : Array.from(
            new Map(
                (catalogs.estados || [])
                    .filter(item => item.pais)
                    .map(item => [item.pais.idPais, item.pais])
            ).values()
        );

    const countrySelect = $('#paises-domicilio');

    if (countrySelect) {
        countrySelect.innerHTML =
            option('', 'Seleccione un país...') +
            countries.map(
                item => option(item.idPais, item.nombre)
            ).join('');
    }

    updateStatesOptions('');
}

function fillGeneralStatuses() {
    document.querySelectorAll('.general-status').forEach(select => {
        select.innerHTML = statusOptions(admin.generalStatuses || []);
    });
}

// ======================================================
// FORMULARIO DE EMPLEADOS
// ======================================================


// ======================================================
// NAVEGACION DEL FORMULARIO DE EMPLEADOS
// ======================================================

let employeeFormStep = 0;

function showEmployeeFormStep(step) {
    const form = $('#edit-form');
    if (!form) return;

    employeeFormStep = step === 1 ? 1 : 0;

    form.querySelectorAll('.employee-form-step').forEach(section => {
        const active = Number(section.dataset.step) === employeeFormStep;

        section.hidden = !active;
        section.style.display = active ? '' : 'none';
        section.classList.toggle('active', active);
    });

    form.querySelectorAll('[data-employee-step]').forEach(tab => {
        const active = Number(tab.dataset.employeeStep) === employeeFormStep;

        tab.classList.toggle('active', active);

        if (active) {
            tab.setAttribute('aria-current', 'step');
        } else {
            tab.removeAttribute('aria-current');
        }
    });

    const previous = $('#employee-prev');
    const next = $('#employee-next');
    const save = $('#employee-save');

    if (previous) {
        previous.hidden = employeeFormStep === 0;
        previous.style.display = previous.hidden ? 'none' : '';
    }

    if (next) {
        next.hidden = employeeFormStep === 1;
        next.style.display = next.hidden ? 'none' : '';
    }

    if (save) {
        save.hidden = employeeFormStep !== 1;
        save.style.display = save.hidden ? 'none' : '';
    }
}

function validateEmployeeStep(step) {
    const form = $('#edit-form');
    const section = form?.querySelector(
        `.employee-form-step[data-step="${step}"]`
    );

    if (!section) return false;

    showEmployeeFormStep(step);

    const fields = [
        ...section.querySelectorAll('input, select, textarea')
    ];

    for (const field of fields) {
        if (!field.disabled && !field.checkValidity()) {
            field.reportValidity();
            return false;
        }
    }

    return true;
}

function bindEmployeeFormSteps() {
    const form = $('#edit-form');
    if (!form) return;

    form.noValidate = true;
    showEmployeeFormStep(0);

    $('#employee-next')?.addEventListener('click', () => {
        if (validateEmployeeStep(0)) {
            showEmployeeFormStep(1);
        }
    });

    $('#employee-prev')?.addEventListener('click', () => {
        showEmployeeFormStep(0);
    });

    form.querySelectorAll('[data-employee-step]').forEach(tab => {
        tab.addEventListener('click', () => {
            const target = Number(tab.dataset.employeeStep);

            if (target === 0 || validateEmployeeStep(0)) {
                showEmployeeFormStep(target);
            }
        });
    });
}

function openEditor(employee) {
    editingId = employee?.idEmpleado || null;

    const form = $('#edit-form');
    if (!form) return;

    form.reset();

    const title = $('#form-title');

    if (title) {
        title.textContent = employee
            ? 'Editar empleado'
            : 'Nuevo empleado';
    }

    fillEmployeeCatalogs();

    if (employee) {
        Object.keys(employee).forEach(key => {
            if (form.elements[key]) {
                form.elements[key].value = employee[key] ?? '';
            }
        });

        const relations = {
            idSexo: employee.sexo?.idSexo,
            idDepto: employee.departamento?.idDepto,
            idPuesto: employee.puesto?.idPuesto,
            idEstatus: employee.estatus?.idEstatus
        };

        Object.entries(relations).forEach(([key, value]) => {
            if (form.elements[key] && value != null) {
                form.elements[key].value = value;
            }
        });

        const address = employee.domicilio || {};

        ['calle', 'codigoPostal', 'numeroExterior', 'numeroInterior']
            .forEach(key => {
                if (form.elements[key]) {
                    form.elements[key].value = address[key] || '';
                }
            });

        const idPais = address.estado?.pais?.idPais || '';

        if (form.elements.idPais) {
            form.elements.idPais.value = idPais;
        }

        updateStatesOptions(idPais);

        if (form.elements.idEstado && address.estado) {
            form.elements.idEstado.value = address.estado.idEstado;
        }

        setStatusFieldState(form, true);

    } else {
        if (form.elements.idSexo && catalogs.sexos?.length) {
            form.elements.idSexo.value = catalogs.sexos[0].idSexo;
        }

        if (form.elements.idEstatus) {
            form.elements.idEstatus.value = 'ACTIVO';
        }

        updateStatesOptions('');
        setStatusFieldState(form, false);
    }

    showEmployeeFormStep(0);

    const dialog = $('#edit-dialog');

    if (dialog && !dialog.open) {
        dialog.showModal();
    }
}

async function saveEmployee(event) {
    event.preventDefault();

    if (!validateEmployeeStep(0) || !validateEmployeeStep(1)) return;

    const payload = formData(event.target);

    const phone = String(payload.telefonoCelular || '')
        .replace(/\D/g, '');

    if (phone.length !== 10) {
        alert('El teléfono celular debe contener exactamente 10 dígitos.');
        return;
    }

    payload.telefonoCelular = phone;

    if (payload.fechaNacimiento) {
        const birth = new Date(`${payload.fechaNacimiento}T00:00:00`);
        const today = new Date();

        let age = today.getFullYear() - birth.getFullYear();

        const birthdayPassed =
            today.getMonth() > birth.getMonth() ||
            (today.getMonth() === birth.getMonth() &&
             today.getDate() >= birth.getDate());

        if (!birthdayPassed) age--;

        if (!Number.isFinite(age) || age < 18) {
            alert('La persona debe tener al menos 18 años.');
            return;
        }
    }

    if (!editingId) {
        payload.idEstatus = payload.idEstatus || 'ACTIVO';
    }

    try {
        const response = await request(
            editingId ? `${api}/${editingId}` : api,
            editingId ? 'PUT' : 'POST',
            payload
        );

        if (!response.ok) {
            throw Error(await response.text() || 'No fue posible guardar.');
        }

        $('#edit-dialog')?.close();
        await load();

    } catch (error) {
        alert(error.message);
    }
}

// ======================================================
// DETALLE DE EMPLEADOS
// ======================================================

async function openDetail(employee) {
    if (!employee) return;

    let courses = [];

    try {
        const response = await request(
            `resources/empleado-cursos/empleado/${employee.idEmpleado}`
        );

        if (response.ok) {
            courses = await response.json();
        }
    } catch (error) {
        console.error(error);
    }

    const name = $('#detail-name');

    if (name) name.textContent = fullName(employee);

    const detail = $('#employee-detail');

    if (detail) {
        detail.innerHTML = `
            <div class="detail-grid">
                <section class="detail-section">
                    <h3>Datos personales</h3>
                    <p>${escapeHtml(fullName(employee))}</p>
                    <p>Nacimiento: ${escapeHtml(employee.fechaNacimiento || 'N/A')}</p>
                    <p>Sexo: ${escapeHtml(employee.sexo?.descripcion || 'N/A')}</p>
                </section>

                <section class="detail-section">
                    <h3>Contacto y puesto</h3>
                    <p>${escapeHtml(employee.correoInstitucional || 'N/A')}</p>
                    <p>${escapeHtml(employee.departamento?.descripcion || 'N/A')}</p>
                    <p>${escapeHtml(employee.puesto?.descripcionPuesto || 'N/A')}</p>
                    <p>Estatus: ${escapeHtml(employee.estatus?.idEstatus || 'N/A')}</p>
                </section>

                <section class="detail-section">
                    <h3>Domicilio</h3>
                    <p>
                        ${escapeHtml(employee.domicilio?.calle || '')},
                        ${escapeHtml(employee.domicilio?.estado?.nombre || '')},
                        ${escapeHtml(employee.domicilio?.estado?.pais?.nombre || '')}
                    </p>
                </section>

                <section class="detail-section detail-courses">
                    <h3>Cursos registrados</h3>
                    ${courses.length
                        ? courses.map(item => `
                            <p>
                                <strong>${escapeHtml(item.curso?.nombre || '')}</strong>
                                ${escapeHtml(item.fechaInicio || '')} -
                                ${escapeHtml(item.fechaFin || 'En curso')}
                            </p>
                        `).join('')
                        : '<p class="muted">No tiene cursos registrados.</p>'
                    }
                </section>
            </div>
        `;
    }

    const dialog = $('#detail-dialog');

    if (dialog && !dialog.open) {
        dialog.showModal();
    }
}

// ======================================================
// EVENTOS DE EMPLEADOS
// ======================================================

function bindEmployeeEvents() {
    $('#search-employees')?.addEventListener('click', searchEmployees);

    $('#clear-filters')?.addEventListener('click', clearEmployeeFilters);

    ['#filter-numero', '#employee-search'].forEach(selector => {
        $(selector)?.addEventListener('keydown', event => {
            if (event.key === 'Enter') {
                event.preventDefault();
                searchEmployees();
            }
        });
    });

    $('#prev-page')?.addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            renderEmployees();
        }
    });

    $('#next-page')?.addEventListener('click', () => {
        currentPage++;
        renderEmployees();
    });

    $('#employees')?.addEventListener('click', async event => {
        const button = event.target.closest(
            'button[data-edit], button[data-detail], button[data-delete]'
        );

        if (!button) return;

        const id = Number(
            button.dataset.edit ||
            button.dataset.detail ||
            button.dataset.delete
        );

        const employee = employees.find(
            item => Number(item.idEmpleado) === id
        );

        if (!employee) return;

        if (button.dataset.edit) {
            openEditor(employee);
        }

        if (button.dataset.detail) {
            await openDetail(employee);
        }

        if (button.dataset.delete) {
            if (!confirm('¿Dar de baja a este empleado?')) return;

            try {
                const response = await request(`${api}/${id}`, 'DELETE');

                if (!response.ok) {
                    throw Error(
                        await response.text() || 'No fue posible dar de baja.'
                    );
                }

                await load();

            } catch (error) {
                alert(error.message);
            }
        }
    });

    $('#paises-domicilio')?.addEventListener('change', event => {
        updateStatesOptions(event.target.value);
    });

    $('#edit-form')?.addEventListener('submit', saveEmployee);

    $('#new-employee')?.addEventListener('click', () => {
        openEditor();
    });

    $('#cancel')?.addEventListener('click', () => {
        $('#edit-dialog')?.close();
    });

    $('#close-detail')?.addEventListener('click', () => {
        $('#detail-dialog')?.close();
    });
}

// ======================================================
// CARGA DE CATALOGOS Y CURSOS
// ======================================================

async function loadAdmin() {
    try {
        const urls = [
            'resources/catalogos/cursos',
            'resources/catalogos/paises',
            'resources/catalogos/estados',
            'resources/catalogos/departamentos',
            'resources/catalogos/puestos',
            'resources/catalogos/sexos',
            'resources/catalogos/estatus?categoria=GENERAL',
            'resources/empleado-cursos'
        ];

        const responses = await Promise.all(
            urls.map(url => request(url))
        );

        if (responses.some(response => !response.ok)) {
            throw Error('No se pudieron cargar los catálogos.');
        }

        [
            admin.courses,
            admin.countries,
            admin.states,
            admin.departments,
            admin.positions,
            admin.sexos,
            admin.generalStatuses,
            admin.assignments
        ] = await Promise.all(
            responses.map(response => response.json())
        );

        renderAdmin();

    } catch (error) {
        console.error(error);
    }
}

function renderAdmin() {
    fillGeneralStatuses();

    const courseStatus = $('#course-status');
    if (courseStatus) {
        courseStatus.innerHTML = statusOptions(admin.generalStatuses || []);
    }

    const assignEmp = $('#assignment-employee');
    if (assignEmp) {
        assignEmp.innerHTML = employees.map(employee =>
            option(employee.idEmpleado, employeeLabel(employee))
        ).join('');
    }

    const assignCourse = $('#assignment-course');
    if (assignCourse) {
        assignCourse.innerHTML = (admin.courses || [])
            .filter(course => course.idEstatus === 'ACTIVO')
            .map(course => option(course.idCurso, course.nombre))
            .join('');
    }

    const stateCountry = $('#state-country');
    if (stateCountry) {
        stateCountry.innerHTML = (admin.countries || [])
            .filter(country => country.idEstatus === 'ACTIVO')
            .map(country => option(country.idPais, country.nombre))
            .join('');
    }

    const tableConfigs = [
        {
            selector: '#courses-body',
            data: admin.courses,
            render: item => `
                <tr>
                    <td>${escapeHtml(item.nombre)}
                        <br><small>${escapeHtml(item.descripcion || '')}</small>
                    </td>
                    <td><span class="status ${escapeHtml(item.idEstatus)}">${escapeHtml(item.idEstatus)}</span></td>
                    <td class="actions">
                        <button data-edit-course="${escapeHtml(item.idCurso)}">Editar</button>
                        <button data-toggle-course="${escapeHtml(item.idCurso)}">
                            ${item.idEstatus === 'ACTIVO' ? 'Desactivar' : 'Activar'}
                        </button>
                    </td>
                </tr>`
        },
        {
            selector: '#countries-body',
            data: admin.countries,
            render: item => catalogRow(
                [item.idPais, item.nombre, statusBadge(item.idEstatus)],
                'country', item.idPais
            )
        },
        {
            selector: '#states-body',
            data: admin.states,
            render: item => catalogRow(
                [item.idEstado, item.nombre, item.pais?.nombre || '',
                 statusBadge(item.idEstatus)],
                'state', item.idEstado
            )
        },
        {
            selector: '#departments-body',
            data: admin.departments,
            render: item => catalogRow(
                [item.descripcion, statusBadge(item.idEstatus)],
                'department', item.idDepto
            )
        },
        {
            selector: '#positions-body',
            data: admin.positions,
            render: item => catalogRow(
                [item.descripcionPuesto, statusBadge(item.idEstatus)],
                'position', item.idPuesto
            )
        },
        {
            selector: '#sexes-body',
            data: admin.sexos,
            render: item => catalogRow(
                [item.idSexo, item.descripcion],
                'sexo', item.idSexo, 'Eliminar'
            )
        }
    ];

    tableConfigs.forEach(config => {
        const table = $(config.selector);
        if (table) {
            table.innerHTML = (config.data || [])
                .map(config.render).join('');
        }
    });

    const statuses = [
        ...(catalogs.estatuses || []),
        ...(admin.generalStatuses || [])
    ].filter((item, index, list) =>
        list.findIndex(other => other.idEstatus === item.idEstatus) === index
    );

    const statusesBody = $('#statuses-body');

    if (statusesBody) {
        statusesBody.innerHTML = statuses.map(item => `
            <tr>
                <td>${escapeHtml(item.idEstatus)}</td>
                <td>${escapeHtml(item.descripcion)}</td>
                <td>${escapeHtml(item.categoria)}</td>
                <td>
                    <button data-edit-status="${escapeHtml(item.idEstatus)}">
                        Editar
                    </button>
                </td>
            </tr>
        `).join('');
    }

    const assignmentsBody = $('#assignments-body');

    if (assignmentsBody) {
        assignmentsBody.innerHTML = (admin.assignments || [])
            .map(item => `
                <tr>
                    <td>${escapeHtml(employeeLabel(item.empleado))}</td>
                    <td>${escapeHtml(item.curso?.nombre || '')}</td>
                    <td>${escapeHtml(item.fechaInicio || '')}</td>
                    <td>${escapeHtml(item.fechaFin || 'En curso')}</td>
                </tr>
            `).join('');
    }
}

function statusBadge(status) {
    return `<span class="status ${escapeHtml(status)}">${escapeHtml(status)}</span>`;
}

function catalogRow(cells, type, id, deleteLabel = 'Desactivar') {
    const formatted = cells.map(cell => {
        const value = String(cell ?? '');
        return `<td>${value.startsWith('<span class="status ') ? value : escapeHtml(value)}</td>`;
    }).join('');

    return `
        <tr>
            ${formatted}
            <td class="actions">
                <button data-edit-${type}="${escapeHtml(id)}">Editar</button>
                <button class="danger" data-delete-${type}="${escapeHtml(id)}">
                    ${deleteLabel}
                </button>
            </td>
        </tr>
    `;
}

// ======================================================
// GUARDAR Y DESACTIVAR CATALOGOS
// ======================================================

async function saveCatalog(form, path) {
    const data = formData(form);
    const id = data.editingId;

    delete data.editingId;

    if (path !== 'sexos' && !data.idEstatus) {
        data.idEstatus = 'ACTIVO';
    }

    try {
        const response = await request(
            `resources/catalogos/${path}${id ? `/${id}` : ''}`,
            id ? 'PUT' : 'POST',
            data
        );

        if (!response.ok) {
            throw Error(await response.text() || 'No fue posible guardar.');
        }

        resetForm(form);
        await loadAdmin();

    } catch (error) {
        alert(error.message);
    }
}

async function toggle(path, id) {
    if (!confirm('¿Deseas desactivar este registro?')) return;

    try {
        const response = await request(
            `resources/catalogos/${path}/${id}`, 'DELETE'
        );

        if (!response.ok) {
            throw Error(await response.text() || 'No fue posible desactivar.');
        }

        await loadAdmin();

    } catch (error) {
        alert(error.message);
    }
}

// ======================================================
// CURSOS
// ======================================================

function bindCourseEvents() {
    $('#course-form')?.addEventListener('submit', saveCourse);
    $('#courses-body')?.addEventListener('click', handleCourseAction);
    $('#assignment-form')?.addEventListener('submit', assignCourse);
    $('#ongoing-course')?.addEventListener('change', toggleCourseEndDate);

    const endDate = $('#assignment-form [name="fechaFin"]');
    if (endDate) endDate.disabled = true;
}

async function saveCourse(event) {
    event.preventDefault();

    const data = formData(event.target);
    const id = data.idCurso;

    delete data.idCurso;

    data.idEstatus = id
        ? data.idEstatus
        : data.idEstatus || 'ACTIVO';

    const response = await request(
        `resources/catalogos/cursos${id ? `/${id}` : ''}`,
        id ? 'PUT' : 'POST',
        data
    );

    if (!response.ok) {
        alert(await response.text() || 'No fue posible guardar el curso.');
        return;
    }

    event.target.reset();
    setStatusFieldState(event.target, false);
    await loadAdmin();
}

function handleCourseAction(event) {
    const button = event.target.closest('button');
    if (!button) return;

    const id = Number(
        button.dataset.editCourse || button.dataset.toggleCourse
    );

    const course = (admin.courses || [])
        .find(item => Number(item.idCurso) === id);

    if (!course) return;

    if (button.dataset.editCourse) {
        const form = $('#course-form');
        if (!form) return;

        form.elements.idCurso.value = course.idCurso;
        form.elements.nombre.value = course.nombre;
        form.elements.descripcion.value = course.descripcion || '';
        form.elements.idEstatus.value = course.idEstatus;

        setStatusFieldState(form, true);
    }

    if (button.dataset.toggleCourse) {
        request(`resources/catalogos/cursos/${id}`, 'PUT', {
            nombre: course.nombre,
            descripcion: course.descripcion,
            idEstatus: course.idEstatus === 'ACTIVO'
                ? 'INACTIVO'
                : 'ACTIVO'
        }).then(response => {
            if (!response.ok) {
                throw Error('No fue posible actualizar el curso.');
            }
            return loadAdmin();
        }).catch(error => alert(error.message));
    }
}

async function assignCourse(event) {
    event.preventDefault();

    const data = formData(event.target);
    const ongoing = $('#ongoing-course');

    if (ongoing?.checked) {
        data.fechaFin = null;
    }

    const response = await request(
        'resources/empleado-cursos', 'POST', data
    );

    if (!response.ok) {
        alert(await response.text() || 'No fue posible asignar el curso.');
        return;
    }

    event.target.reset();

    if (ongoing) ongoing.checked = true;

    const endDate = $('#assignment-form [name="fechaFin"]');
    if (endDate) endDate.disabled = true;

    await loadAdmin();
}

function toggleCourseEndDate(event) {
    const field = $('#assignment-form [name="fechaFin"]');
    if (!field) return;

    field.disabled = event.target.checked;

    if (event.target.checked) {
        field.value = '';
    }
}

// ======================================================
// EVENTOS DE CATALOGOS
// ======================================================

function bindCatalogEvents() {
    const forms = {
        country: 'paises',
        state: 'estados',
        department: 'departamentos',
        position: 'puestos'
    };

    Object.entries(forms).forEach(([name, path]) => {
        $(`#${name}-form`)?.addEventListener('submit', event => {
            event.preventDefault();
            saveCatalog(event.target, path);
        });
    });

    $('#sexo-form')?.addEventListener('submit', saveSexo);
    $('#status-form')?.addEventListener('submit', saveStatus);

    document.querySelectorAll('[id^="cancel-"]').forEach(button => {
        button.addEventListener('click', () => {
            const form =
                button.closest('article')?.querySelector('form') ||
                document.getElementById(
                    button.id.replace('cancel-', '') + '-form'
                );

            if (form) resetForm(form);
        });
    });
}

async function saveSexo(event) {
    event.preventDefault();

    const data = formData(event.target);
    const id = data.editingId;
    delete data.editingId;

    const response = await request(
        id ? `resources/catalogos/sexos/${id}` : 'resources/catalogos/sexos',
        id ? 'PUT' : 'POST',
        data
    );

    if (!response.ok) {
        alert(await response.text() || 'No fue posible guardar el sexo.');
        return;
    }

    resetForm(event.target);
    await loadAdmin();
}

async function saveStatus(event) {
    event.preventDefault();

    const data = formData(event.target);
    const id = data.editingId;
    delete data.editingId;

    const response = await request(
        id ? `resources/catalogos/estatus/${id}` : 'resources/catalogos/estatus',
        id ? 'PUT' : 'POST',
        data
    );

    if (!response.ok) {
        alert(await response.text() || 'No fue posible guardar el estatus.');
        return;
    }

    resetForm(event.target);
    event.target.elements.idEstatus.disabled = false;

    await load();
}

async function deleteSexo(id) {
    if (!confirm('¿Deseas eliminar este sexo?')) return;

    const response = await request(
        `resources/catalogos/sexos/${id}`, 'DELETE'
    );

    if (!response.ok) {
        alert(await response.text() || 'No fue posible eliminar el sexo.');
        return;
    }

    await loadAdmin();
}

function editStatus(id) {
    const item = [
        ...(catalogs.estatuses || []),
        ...(admin.generalStatuses || [])
    ].find(status => status.idEstatus === id);

    if (!item) return;

    const form = $('#status-form');
    if (!form) return;

    form.elements.editingId.value = item.idEstatus;
    form.elements.idEstatus.value = item.idEstatus;
    form.elements.idEstatus.disabled = true;
    form.elements.descripcion.value = item.descripcion;
    form.elements.categoria.value = item.categoria;
}

function bindCatalogTableEvents() {
    document.querySelectorAll('.catalog-panel').forEach(panel => {
        panel.addEventListener('click', event => {
            const button = event.target.closest('button');
            if (!button) return;

            const data = button.dataset;

            const mappings = [
                ['Country', '#country-form', admin.countries, 'idPais'],
                ['State', '#state-form', admin.states, 'idEstado'],
                ['Department', '#department-form', admin.departments, 'idDepto'],
                ['Position', '#position-form', admin.positions, 'idPuesto'],
                ['Sexo', '#sexo-form', admin.sexos, 'idSexo']
            ];

            for (const [name, selector, items, key] of mappings) {
                const editKey = `edit${name}`;
                const deleteKey = `delete${name}`;

                if (data[editKey] !== undefined) {
                    const id = data[editKey];
                    const item = (items || []).find(
                        row => String(row[key]) === String(id)
                    );
                    editForm($(selector), item, id);
                    return;
                }

                if (data[deleteKey] !== undefined) {
                    const id = data[deleteKey];

                    if (name === 'Sexo') {
                        deleteSexo(id);
                    } else {
                        const paths = {
                            Country: 'paises',
                            State: 'estados',
                            Department: 'departamentos',
                            Position: 'puestos'
                        };

                        toggle(paths[name], id);
                    }
                    return;
                }
            }

            if (data.editStatus) {
                editStatus(data.editStatus);
            }
        });
    });
}

// ======================================================
// BUSQUEDA EN TABLAS DE CATALOGOS
// ======================================================

function bindTableSearch() {
    document.querySelectorAll('[data-table-filter]').forEach(input => {
        input.addEventListener('input', () => {
            const query = (input.value || '').trim().toLowerCase();
            const table = document.getElementById(
                input.dataset.tableFilter
            );

            table?.querySelectorAll('tr').forEach(row => {
                row.style.display =
                    !query || row.textContent.toLowerCase().includes(query)
                        ? ''
                        : 'none';
            });
        });
    });
}

// ======================================================
// NAVEGACION
// ======================================================

function bindNavigationEvents() {
    const themeByView = {
        'employees-view': 'overworld',
        'courses-view': 'nether',
        'catalogs-view': 'end'
    };

    document.querySelectorAll('[data-view]').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelectorAll('.tabs button').forEach(item => {
                item.classList.remove('active');
            });

            document.querySelectorAll('.view').forEach(item => {
                item.classList.remove('active');
            });

            button.classList.add('active');

            const target = $(`#${button.dataset.view}`);

            if (target) {
                target.classList.add('active');
            }

            document.body.dataset.theme =
                themeByView[button.dataset.view] || 'nether';

            if (button.dataset.view !== 'employees-view') {
                loadAdmin();
            }
        });
    });

    document.querySelectorAll('[data-catalog]').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelectorAll('.subtabs button').forEach(item => {
                item.classList.remove('active');
            });

            document.querySelectorAll('.catalog-panel').forEach(item => {
                item.classList.remove('active');
            });

            button.classList.add('active');

            const target = $(`#${button.dataset.catalog}`);

            if (target) target.classList.add('active');
        });
    });

    $('#reload-courses')?.addEventListener('click', loadAdmin);
    $('#reload-catalogs')?.addEventListener('click', loadAdmin);
}

// ======================================================
// INICIALIZACION
// ======================================================

function bindUIEvents() {
    bindEmployeeFormSteps();
    bindEmployeeEvents();
    bindCourseEvents();
    bindCatalogEvents();
    bindCatalogTableEvents();
    bindNavigationEvents();
    bindTableSearch();

    [
        'country',
        'state',
        'department',
        'position',
        'sexo',
        'status'
    ].forEach(name => resetForm($(`#${name}-form`)));

    document.body.dataset.theme = 'overworld';

    load();
}

// ======================================================
// CARGA DE VISTAS Y FORMULARIOS
// ======================================================

async function loadFragments() {
    const shell = $('#app-shell');
    if (!shell) return;

    const views = [
        'views/employees-view.html',
        'views/courses-view.html',
        'views/catalogs-view.html'
    ];

    const forms = {
        'employee-form-slot': 'forms/employee-form.html',
        'course-form-slot': 'forms/course-form.html',
        'assignment-form-slot': 'forms/assignment-form.html',
        'country-form-slot': 'forms/country-form.html',
        'state-form-slot': 'forms/state-form.html',
        'department-form-slot': 'forms/department-form.html',
        'position-form-slot': 'forms/position-form.html',
        'sexo-form-slot': 'forms/sexo-form.html',
        'status-form-slot': 'forms/status-form.html'
    };

    for (const view of views) {
        try {
            const response = await fetch(view);

            if (response.ok) {
                shell.insertAdjacentHTML(
                    'beforeend',
                    await response.text()
                );
            } else {
                console.error(`No se pudo cargar ${view}`);
            }
        } catch (error) {
            console.error(`Error cargando ${view}:`, error);
        }
    }

    for (const [slotId, path] of Object.entries(forms)) {
        try {
            const response = await fetch(path);
            const slot = document.getElementById(slotId);

            if (slot && response.ok) {
                slot.innerHTML = await response.text();
            }
        } catch (error) {
            console.error(`Error cargando ${path}:`, error);
        }
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadFragments();
    bindUIEvents();
});
