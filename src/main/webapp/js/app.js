const api = 'resources/empleados';
const $ = selector => document.querySelector(selector);
const option = (value, label) => `<option value="${value}">${label}</option>`;

let employees = [];
let catalogs = { departamentos: [], puestos: [], sexos: [], estatuses: [], estados: [] };
let admin = {};
let editingId;

const fullName = employee => [employee.nombre, employee.apellidoPaterno, employee.apellidoMaterno].filter(Boolean).join(' ');
const employeeLabel = employee => `${fullName(employee)} (#${employee.idEmpleado})`;
const formData = form => Object.fromEntries(new FormData(form));
const request = (url, method = 'GET', body) => fetch(url, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined
});
const statusOptions = list => list.map(item => option(item.idEstatus, `${item.idEstatus} - ${item.descripcion}`)).join('');

function setStatusFieldState(form, visible) {
    const field = form.querySelector('[data-status-field]');
    if (!field) return;
    const select = field.querySelector('select');
    if (!select) return;
    field.style.display = visible ? '' : 'none';
    select.disabled = !visible;
    select.required = visible;
    if (!visible) select.value = 'ACTIVO';
}

function bindTableSearch() {
    document.querySelectorAll('[data-table-filter]').forEach(input => {
        input.addEventListener('input', () => {
            const query = (input.value || '').trim().toLowerCase();
            const rows = document.querySelectorAll(`#${CSS.escape(input.dataset.tableFilter)} tr`);
            rows.forEach(row => row.style.display = !query || row.textContent.toLowerCase().includes(query) ? '' : 'none');
        });
    });
    const searchInput = $('#employee-search');
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            const query = searchInput.value.trim().toLowerCase();
            $('#employees').querySelectorAll('tr').forEach(row => row.style.display = !query || row.textContent.toLowerCase().includes(query) ? '' : 'none');
        });
    }
}

async function load() {
    try {
        const responses = await Promise.all([request(api), request(`${api}/catalogos`)]);
        if (responses.some(response => !response.ok)) throw Error('No se pudieron cargar los empleados.');
        employees = await responses[0].json();
        catalogs = await responses[1].json();
        renderEmployees();
        fillEmployeeCatalogs();
        await loadAdmin();
        bindTableSearch();
    } catch (error) {
        $('#employees').innerHTML = `<tr><td class="error" colspan="6">${error.message}</td></tr>`;
    }
}

async function loadAdmin() {
    try {
        const urls = [
            'resources/catalogos/cursos', 'resources/catalogos/paises', 'resources/catalogos/estados',
            'resources/catalogos/departamentos', 'resources/catalogos/puestos', 'resources/catalogos/sexos',
            'resources/catalogos/estatus?categoria=GENERAL', 'resources/empleado-cursos'
        ];
        const responses = await Promise.all(urls.map(url => request(url)));
        if (responses.some(response => !response.ok)) throw Error('No se pudieron cargar los catalogos.');
        [admin.courses, admin.countries, admin.states, admin.departments, admin.positions,
            admin.sexos, admin.generalStatuses, admin.assignments] = await Promise.all(responses.map(response => response.json()));
        renderAdmin();
    } catch (error) {
        console.error(error);
    }
}

function renderEmployees() {
    $('#count').textContent = `${employees.length} empleados`;
    $('#employees').innerHTML = employees.length ? employees.map(employee => `<tr>
        <td><div class="name">${fullName(employee)}</div><small>${employee.correoInstitucional}</small></td>
        <td>${employee.telefonoCelular || 'Sin telefono'}</td><td>${employee.departamento.descripcion}</td>
        <td>${employee.puesto.descripcionPuesto}</td><td><span class="status ${employee.estatus.idEstatus}">${employee.estatus.idEstatus}</span></td>
        <td class="actions"><button data-detail="${employee.idEmpleado}" type="button">Ver detalle</button><button data-edit="${employee.idEmpleado}" type="button">Editar</button><button data-delete="${employee.idEmpleado}" class="danger" type="button">Dar de baja</button></td>
    </tr>`).join('') : '<tr><td class="empty" colspan="6">No hay empleados registrados.</td></tr>';
}

function fillEmployeeCatalogs() {
    $('#sexos').innerHTML = catalogs.sexos.map(item => option(item.idSexo, item.descripcion)).join('');
    $('#departamentos').innerHTML = catalogs.departamentos.map(item => option(item.idDepto, item.descripcion)).join('');
    $('#puestos').innerHTML = catalogs.puestos.map(item => option(item.idPuesto, item.descripcionPuesto)).join('');
    $('#estatuses').innerHTML = statusOptions(catalogs.estatuses);
    $('#estados-domicilio').innerHTML = catalogs.estados.map(item => option(item.idEstado, `${item.nombre} - ${item.pais.nombre}`)).join('');
}

function fillGeneralStatuses() {
    document.querySelectorAll('.general-status').forEach(select => select.innerHTML = statusOptions(admin.generalStatuses));
}

function renderAdmin() {
    fillGeneralStatuses();
    $('#course-status').innerHTML = statusOptions(admin.generalStatuses);
    $('#assignment-employee').innerHTML = employees.map(employee => option(employee.idEmpleado, employeeLabel(employee))).join('');
    $('#assignment-course').innerHTML = admin.courses.filter(course => course.idEstatus === 'ACTIVO').map(course => option(course.idCurso, course.nombre)).join('');
    $('#state-country').innerHTML = admin.countries.filter(country => country.idEstatus === 'ACTIVO').map(country => option(country.idPais, country.nombre)).join('');
    $('#courses-body').innerHTML = admin.courses.map(course => `<tr><td>${course.nombre}<br><small>${course.descripcion || ''}</small></td><td><span class="status ${course.idEstatus}">${course.idEstatus}</span></td><td class="actions"><button data-edit-course="${course.idCurso}" type="button">Editar</button><button data-toggle-course="${course.idCurso}" type="button">${course.idEstatus === 'ACTIVO' ? 'Desactivar' : 'Activar'}</button></td></tr>`).join('');
    $('#countries-body').innerHTML = admin.countries.map(item => `<tr><td>${item.idPais}</td><td>${item.nombre}</td><td><span class="status ${item.idEstatus}">${item.idEstatus}</span></td><td class="actions"><button data-edit-country="${item.idPais}" type="button">Editar</button><button data-delete-country="${item.idPais}" class="danger" type="button">Desactivar</button></td></tr>`).join('');
    $('#states-body').innerHTML = admin.states.map(item => `<tr><td>${item.idEstado}</td><td>${item.nombre}</td><td>${item.pais.nombre}</td><td><span class="status ${item.idEstatus}">${item.idEstatus}</span></td><td class="actions"><button data-edit-state="${item.idEstado}" type="button">Editar</button><button data-delete-state="${item.idEstado}" class="danger" type="button">Desactivar</button></td></tr>`).join('');
    $('#departments-body').innerHTML = admin.departments.map(item => `<tr><td>${item.descripcion}</td><td><span class="status ${item.idEstatus}">${item.idEstatus}</span></td><td class="actions"><button data-edit-department="${item.idDepto}" type="button">Editar</button><button data-delete-department="${item.idDepto}" class="danger" type="button">Desactivar</button></td></tr>`).join('');
    $('#positions-body').innerHTML = admin.positions.map(item => `<tr><td>${item.descripcionPuesto}</td><td><span class="status ${item.idEstatus}">${item.idEstatus}</span></td><td class="actions"><button data-edit-position="${item.idPuesto}" type="button">Editar</button><button data-delete-position="${item.idPuesto}" class="danger" type="button">Desactivar</button></td></tr>`).join('');
    $('#sexes-body').innerHTML = (admin.sexos || []).map(item => `<tr><td>${item.idSexo}</td><td>${item.descripcion}</td><td class="actions"><button data-edit-sexo="${item.idSexo}" type="button">Editar</button><button data-delete-sexo="${item.idSexo}" class="danger" type="button">Eliminar</button></td></tr>`).join('');
    const statuses = [...catalogs.estatuses, ...admin.generalStatuses].filter((item, index, list) => list.findIndex(other => other.idEstatus === item.idEstatus) === index);
    $('#statuses-body').innerHTML = statuses.map(item => `<tr><td>${item.idEstatus}</td><td>${item.descripcion}</td><td>${item.categoria}</td><td><button data-edit-status="${item.idEstatus}" type="button">Editar</button></td></tr>`).join('');
    $('#assignments-body').innerHTML = admin.assignments.map(item => `<tr><td>${employeeLabel(item.empleado)}</td><td>${item.curso.nombre}</td><td>${item.fechaInicio || ''}</td><td>${item.fechaFin || 'En curso'}</td></tr>`).join('');
}

function resetForm(form) {
    form.reset();
    if (form.elements.editingId) form.elements.editingId.value = '';
    if (form.elements.idSexo && catalogs.sexos?.length) form.elements.idSexo.value = catalogs.sexos[0].idSexo;
    setStatusFieldState(form, false);
    if (form.elements.idEstatus) form.elements.idEstatus.value = 'ACTIVO';
}

function editForm(form, item, id) {
    form.reset();
    if (form.elements.editingId) form.elements.editingId.value = id;
    Object.keys(item).forEach(key => { if (form.elements[key]) form.elements[key].value = item[key] ?? ''; });
    if (item.pais && form.elements.idPais) form.elements.idPais.value = item.pais.idPais;
    fillGeneralStatuses();
    if (form.elements.idEstatus) {
        form.elements.idEstatus.value = item.idEstatus;
        setStatusFieldState(form, true);
    }
}

async function saveCatalog(form, path) {
    const data = formData(form);
    const id = data.editingId;
    delete data.editingId;
    if (path !== 'sexos' && !data.idEstatus) data.idEstatus = 'ACTIVO';
    const response = await request(`resources/catalogos/${path}${id ? `/${id}` : ''}`, id ? 'PUT' : 'POST', data);
    if (!response.ok) return alert(await response.text() || 'No fue posible guardar.');
    resetForm(form);
    loadAdmin();
}

async function toggle(path, id) {
    if (!confirm('¿Deseas desactivar este registro?')) return;
    const response = await request(`resources/catalogos/${path}/${id}`, 'DELETE');
    if (!response.ok) alert(await response.text() || 'No fue posible desactivar.');
    loadAdmin();
}

function openEditor(employee) {
    editingId = employee?.idEmpleado;
    const form = $('#edit-form');
    form.reset();
    $('#form-title').textContent = employee ? 'Editar empleado' : 'Nuevo empleado';
    fillEmployeeCatalogs();
    if (employee) {
        Object.keys(employee).forEach(key => { if (form.elements[key]) form.elements[key].value = employee[key] ?? ''; });
        form.elements.idSexo.value = employee.sexo.idSexo;
        form.elements.idDepto.value = employee.departamento.idDepto;
        form.elements.idPuesto.value = employee.puesto.idPuesto;
        form.elements.idEstatus.value = employee.estatus.idEstatus;
        form.elements.calle.value = employee.domicilio.calle || '';
        form.elements.codigoPostal.value = employee.domicilio.codigoPostal || '';
        form.elements.numeroExterior.value = employee.domicilio.numeroExterior || '';
        form.elements.numeroInterior.value = employee.domicilio.numeroInterior || '';
        form.elements.idEstado.value = employee.domicilio.estado.idEstado;
        setStatusFieldState(form, true);
    } else {
        if (form.elements.idSexo && catalogs.sexos?.length) form.elements.idSexo.value = catalogs.sexos[0].idSexo;
        form.elements.idEstatus.value = 'ACTIVO';
        setStatusFieldState(form, false);
    }
    $('#edit-dialog').showModal();
}

async function openDetail(employee) {
    const response = await request(`resources/empleado-cursos/empleado/${employee.idEmpleado}`);
    const courses = response.ok ? await response.json() : [];
    $('#detail-name').textContent = fullName(employee);
    $('#employee-detail').innerHTML = `<div class="detail-grid"><section class="detail-section"><h3>Datos personales</h3><p>${fullName(employee)}</p><p>Nacimiento: ${employee.fechaNacimiento}</p><p>Sexo: ${employee.sexo.descripcion}</p></section><section class="detail-section"><h3>Contacto y puesto</h3><p>${employee.correoInstitucional}</p><p>${employee.departamento.descripcion}</p><p>${employee.puesto.descripcionPuesto}</p><p>Estatus: ${employee.estatus.idEstatus}</p></section><section class="detail-section"><h3>Domicilio</h3><p>${employee.domicilio.calle}, ${employee.domicilio.estado.nombre}, ${employee.domicilio.estado.pais.nombre}</p></section><section class="detail-section detail-courses"><h3>Cursos inscritos</h3>${courses.length ? courses.map(item => `<p><strong>${item.curso.nombre}</strong> ${item.fechaInicio} - ${item.fechaFin || 'En curso'}</p>`).join('') : '<p class="muted">No tiene cursos inscritos.</p>'}</section></div>`;
    $('#detail-dialog').showModal();
}

function bindEmployeeEvents() {
    $('#employees').addEventListener('click', event => {
        const id = Number(event.target.dataset.edit || event.target.dataset.detail || event.target.dataset.delete);
        const employee = employees.find(item => item.idEmpleado === id);
        if (event.target.dataset.edit) openEditor(employee);
        if (event.target.dataset.detail) openDetail(employee);
        if (event.target.dataset.delete && confirm('¿Dar de baja a este empleado?')) request(`${api}/${id}`, 'DELETE').then(load);
    });
    $('#edit-form').addEventListener('submit', saveEmployee);
    $('#new-employee').addEventListener('click', () => openEditor());
    $('#refresh').addEventListener('click', load);
    $('#cancel').addEventListener('click', () => $('#edit-dialog').close());
    $('#close-detail').addEventListener('click', () => $('#detail-dialog').close());
}

async function saveEmployee(event) {
    event.preventDefault();
    const payload = formData(event.target);
    const phone = String(payload.telefonoCelular || '').replace(/\D/g, '');
    if (phone.length !== 10) return alert('El teléfono celular debe contener exactamente 10 dígitos.');
    payload.telefonoCelular = phone;
    const birthDate = payload.fechaNacimiento ? new Date(`${payload.fechaNacimiento}T00:00:00`) : null;
    if (birthDate && (birthDate > new Date() || birthDate > new Date(Date.now() - 18 * 365.25 * 24 * 60 * 60 * 1000))) return alert('La persona debe ser mayor o igual a 18 años.');
    if (!editingId) payload.idEstatus = payload.idEstatus || 'ACTIVO';
    const response = await request(editingId ? `${api}/${editingId}` : api, editingId ? 'PUT' : 'POST', payload);
    if (!response.ok) return alert(await response.text() || 'No fue posible guardar.');
    $('#edit-dialog').close();
    load();
}

function bindCourseEvents() {
    $('#course-form').addEventListener('submit', saveCourse);
    $('#courses-body').addEventListener('click', handleCourseAction);
    $('#assignment-form').addEventListener('submit', assignCourse);
    $('#ongoing-course').addEventListener('change', toggleCourseEndDate);
    $('#assignment-form [name="fechaFin"]').disabled = true;
}

async function saveCourse(event) {
    event.preventDefault();
    const data = formData(event.target);
    const id = data.idCurso;
    delete data.idCurso;
    data.idEstatus = id ? data.idEstatus : (data.idEstatus || 'ACTIVO');
    const response = await request(`resources/catalogos/cursos${id ? `/${id}` : ''}`, id ? 'PUT' : 'POST', data);
    if (!response.ok) return alert(await response.text() || 'No fue posible guardar el curso.');
    event.target.reset();
    setStatusFieldState(event.target, false);
    loadAdmin();
}

function handleCourseAction(event) {
    const id = Number(event.target.dataset.editCourse || event.target.dataset.toggleCourse);
    const course = admin.courses.find(item => item.idCurso === id);
    if (!course) return;
    if (event.target.dataset.editCourse) {
        const form = $('#course-form');
        form.elements.idCurso.value = course.idCurso;
        form.elements.nombre.value = course.nombre;
        form.elements.descripcion.value = course.descripcion || '';
        form.elements.idEstatus.value = course.idEstatus;
        setStatusFieldState(form, true);
    }
    if (event.target.dataset.toggleCourse) request(`resources/catalogos/cursos/${id}`, 'PUT', { nombre: course.nombre, descripcion: course.descripcion, idEstatus: course.idEstatus === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO' }).then(loadAdmin);
}

async function assignCourse(event) {
    event.preventDefault();
    const data = formData(event.target);
    if ($('#ongoing-course').checked) data.fechaFin = null;
    const response = await request('resources/empleado-cursos', 'POST', data);
    if (!response.ok) return alert(await response.text() || 'No fue posible asignar el curso.');
    event.target.reset();
    $('#ongoing-course').checked = true;
    $('#assignment-form [name="fechaFin"]').disabled = true;
    loadAdmin();
}

function toggleCourseEndDate(event) {
    const endDate = $('#assignment-form [name="fechaFin"]');
    endDate.disabled = event.target.checked;
    if (event.target.checked) endDate.value = '';
}

function bindCatalogEvents() {
    const catalogForms = { country: 'paises', state: 'estados', department: 'departamentos', position: 'puestos' };
    Object.entries(catalogForms).forEach(([name, path]) => $(`#${name}-form`).addEventListener('submit', event => { event.preventDefault(); saveCatalog(event.target, path); }));
    $('#sexo-form').addEventListener('submit', saveSexo);
    $('#status-form').addEventListener('submit', saveStatus);
    document.querySelectorAll('[id^="cancel-"]').forEach(button => button.addEventListener('click', () => { const form = button.closest('article')?.querySelector('form') || document.getElementById(button.id.replace('cancel-', '') + '-form'); if (form) resetForm(form); }));
}

async function saveSexo(event) {
    event.preventDefault();
    const data = formData(event.target);
    const id = data.editingId;
    delete data.editingId;
    const response = await request(id ? `resources/catalogos/sexos/${id}` : 'resources/catalogos/sexos', id ? 'PUT' : 'POST', data);
    if (!response.ok) return alert(await response.text() || 'No fue posible guardar el sexo.');
    resetForm(event.target);
    loadAdmin();
}

async function saveStatus(event) {
    event.preventDefault();
    const data = formData(event.target);
    const id = data.editingId;
    delete data.editingId;
    const response = await request(id ? `resources/catalogos/estatus/${id}` : 'resources/catalogos/estatus', id ? 'PUT' : 'POST', data);
    if (!response.ok) return alert(await response.text() || 'No fue posible guardar el estatus.');
    resetForm(event.target);
    event.target.elements.idEstatus.disabled = false;
    load();
}

function bindCatalogTableEvents() {
    document.querySelectorAll('.catalog-panel').forEach(panel => panel.addEventListener('click', event => {
        const target = event.target.dataset;
        if (target.editCountry) editForm($('#country-form'), admin.countries.find(item => item.idPais === target.editCountry), target.editCountry);
        if (target.deleteCountry) toggle('paises', target.deleteCountry);
        if (target.editState) editForm($('#state-form'), admin.states.find(item => item.idEstado === target.editState), target.editState);
        if (target.deleteState) toggle('estados', target.deleteState);
        if (target.editDepartment) editForm($('#department-form'), admin.departments.find(item => item.idDepto === Number(target.editDepartment)), target.editDepartment);
        if (target.deleteDepartment) toggle('departamentos', target.deleteDepartment);
        if (target.editPosition) editForm($('#position-form'), admin.positions.find(item => item.idPuesto === Number(target.editPosition)), target.editPosition);
        if (target.deletePosition) toggle('puestos', target.deletePosition);
        if (target.editSexo) editForm($('#sexo-form'), admin.sexos.find(item => item.idSexo === target.editSexo), target.editSexo);
        if (target.deleteSexo) deleteSexo(target.deleteSexo);
        if (target.editStatus) editStatus(target.editStatus);
    }));
}

async function deleteSexo(id) {
    if (!confirm('¿Deseas eliminar este sexo?')) return;
    const response = await request(`resources/catalogos/sexos/${id}`, 'DELETE');
    if (!response.ok) return alert(await response.text() || 'No fue posible eliminar el sexo.');
    loadAdmin();
}

function editStatus(id) {
    const item = [...catalogs.estatuses, ...admin.generalStatuses].find(status => status.idEstatus === id);
    const form = $('#status-form');
    form.elements.editingId.value = item.idEstatus;
    form.elements.idEstatus.value = item.idEstatus;
    form.elements.idEstatus.disabled = true;
    form.elements.descripcion.value = item.descripcion;
    form.elements.categoria.value = item.categoria;
}

function bindNavigationEvents() {
    const themeByView = { 'employees-view': 'overworld', 'courses-view': 'nether', 'catalogs-view': 'end' };
    document.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', () => {
        document.querySelectorAll('.tabs button').forEach(item => item.classList.remove('active'));
        document.querySelectorAll('.view').forEach(item => item.classList.remove('active'));
        button.classList.add('active');
        $(`#${button.dataset.view}`).classList.add('active');
        document.body.dataset.theme = themeByView[button.dataset.view] || 'nether';
        if (button.dataset.view !== 'employees-view') loadAdmin();
    }));
    document.querySelectorAll('[data-catalog]').forEach(button => button.addEventListener('click', () => {
        document.querySelectorAll('.subtabs button').forEach(item => item.classList.remove('active'));
        document.querySelectorAll('.catalog-panel').forEach(item => item.classList.remove('active'));
        button.classList.add('active');
        $(`#${button.dataset.catalog}`).classList.add('active');
    }));
    $('#reload-courses').addEventListener('click', loadAdmin);
    $('#reload-catalogs').addEventListener('click', loadAdmin);
}

function bindUIEvents() {
    bindEmployeeEvents();
    bindCourseEvents();
    bindCatalogEvents();
    bindCatalogTableEvents();
    bindNavigationEvents();
    ['country', 'state', 'department', 'position', 'sexo', 'status'].forEach(name => resetForm($(`#${name}-form`)));
    document.body.dataset.theme = 'overworld';
    bindTableSearch();
    load();
}

async function loadFragments() {
    const shell = $('#app-shell');
    const views = ['views/employees-view.html', 'views/courses-view.html', 'views/catalogs-view.html'];
    const forms = {
        'employee-form-slot': 'forms/employee-form.html', 'course-form-slot': 'forms/course-form.html',
        'assignment-form-slot': 'forms/assignment-form.html', 'country-form-slot': 'forms/country-form.html',
        'state-form-slot': 'forms/state-form.html', 'department-form-slot': 'forms/department-form.html',
        'position-form-slot': 'forms/position-form.html', 'sexo-form-slot': 'forms/sexo-form.html',
        'status-form-slot': 'forms/status-form.html'
    };
    for (const view of views) {
        const response = await fetch(view);
        shell.insertAdjacentHTML('beforeend', await response.text());
    }
    for (const [slotId, form] of Object.entries(forms)) {
        const response = await fetch(form);
        const slot = document.getElementById(slotId);
        if (slot) slot.innerHTML = await response.text();
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadFragments();
    bindUIEvents();
});
