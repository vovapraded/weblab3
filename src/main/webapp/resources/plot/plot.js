const multiplier = 75;
export function drawFullPlot(R) {
    console.log(`Function drawFullPlot is working. Radius: ${R}`);



    createRectangle(R , R,R);
    createTriangle(R, R / 2,R);

    createQuarterCircle(R/2);
    createPoint(0, 0,"tick",R);
    create4Points(R,R);
    create4Points(R / 2,R);
    // Получаем массив данных из sessionStorage
    const sessionData = JSON.parse(sessionStorage.getItem('formData')) || [];

// Для каждой записи из sessionData создаем точку на графике
    sessionData.forEach(data => {
        // Извлекаем координаты и класс для точки
        const { x, y } = data;
        // Вызываем функцию createPoint для каждого элемента массива
        createPoint(x, y, "point",R);
    });
}// Пример другой точки
function maxByAbsolute(a, b) {
    return Math.abs(a) > Math.abs(b) ? a : b;
}

window.createPoint = function createPoint(cx, cy, clazz, R) {
    console.log("createPoint called with parameters:", { cx, cy, clazz, R }); // Логируем входные параметры

    let svgNamespace = "http://www.w3.org/2000/svg";
    let svg = document.getElementById('mySvg');
    const viewBox = svg.viewBox.baseVal;

    // Создаем круг
    let point = document.createElementNS(svgNamespace, 'circle');
    point.setAttribute('class', clazz);
    point.setAttribute('r', '2'); // Размер точки
    point.setAttribute('cx', cx * multiplier/R + viewBox.width / 2); // Масштабирование и смещение
    point.setAttribute('cy', viewBox.height / 2+ - cy * multiplier/R); // Масштабирование и смещение
    let label = document.createElementNS(svgNamespace, 'text');
    label.setAttribute('class', 'label');
    label.setAttribute('x', cx * multiplier/R + viewBox.width / 2+ 1); // Масштабирование и смещение
    label.setAttribute('y', viewBox.height / 2 - cy * multiplier/R - 5); // Масштабирование и смещение
    console.log("point:", { cx, cy, clazz, R }); // Логируем входные параметры

    // Создаем текст
    if (clazz === "tick"){
        label.textContent = maxByAbsolute(cx, cy); // Подпись для точки
    }else{
        label.textContent = "("+cx+", "+cy+")";
    }



    // Добавляем элементы в SVG
    svg.appendChild(point);
    svg.appendChild(label);
}

function create4Points(shift, R) {
    createPoint(shift, 0, "tick",R); // Пример точки
    createPoint(-shift, 0,"tick",R); // Пример другой точки
    createPoint(0, shift,"tick",R); // Пример точки
    createPoint(0, -shift,"tick",R);
}

function createQuarterCircle(R) {
    let svgNamespace = "http://www.w3.org/2000/svg";
    let svg = document.getElementById('mySvg');

    // Создаем путь для полукруга
    let path = document.createElementNS(svgNamespace, 'path');
    let d = `M 100 100
             L ${100 - multiplier/2} 100
             A ${multiplier/2} ${ multiplier/2} 0 0 0 100 ${100 + multiplier/2}
             Z`;
    path.setAttribute('d', d);
    path.setAttribute("class","figure");
    // Добавляем путь в SVG перед другими элементами
    svg.appendChild(path);
}

function createRectangle(width, height,R) {
    let svgNamespace = "http://www.w3.org/2000/svg";
    let svg = document.getElementById('mySvg');

    // Создаем путь для полукруга
    let path = document.createElementNS(svgNamespace, 'path');
    let d = `M 100 100
             L ${100 + width * multiplier/R} 100
             L ${100 + width * multiplier/R} ${100 - height * multiplier/R}
             L 100 ${100 - height * multiplier/R}

             Z`;
    path.setAttribute('d', d);
    path.setAttribute("class","figure");


    // Добавляем путь в SVG перед другими элементами
    svg.appendChild(path);
}

function createTriangle(bigCatheter, smallCatheter,R) {
    let svgNamespace = "http://www.w3.org/2000/svg";
    let svg = document.getElementById('mySvg');

    // Создаем путь для полукруга
    let path = document.createElementNS(svgNamespace, 'path');
    let d = `M 100 100
             L 100 ${100 - smallCatheter * multiplier/R}
             L ${100 - bigCatheter * multiplier/R} 100

             Z`;
    path.setAttribute('d', d);
    path.setAttribute("class","figure");

    // Добавляем путь в SVG перед другими элементами
    svg.appendChild(path);
}
export function clearPlot() {
    const svg = document.getElementById('mySvg'); // Находим элемент SVG по ID
    if (svg) {
        // Собираем все дочерние элементы в массив, чтобы избежать изменения коллекции во время итерации
        const children = Array.from(svg.children);
        children.forEach(child => {
            // Проверяем, если класс элемента не содержит 'grid', то удаляем его
            if (!child.classList.contains('grid')) {
                svg.removeChild(child);
            }
        });
    }
}
// Получить выбранное значение радио-кнопки
export function getSelectedR() {
    const input = document.getElementById("myForm:r");
    return input.value; // или любое другое значение по умолчанию

}

// Обновление графика на основе выбранного значения
window.updatePlot =  function updatePlot() {
    clearPlot();
    const R = getSelectedR(); // Получаем выбранное значение R
    if (R !== null) {
        drawFullPlot(R); // Передаем значение R в функцию
    } else {
        console.log('No R value selected');
    }
}

// Добавление слушателя событий на изменение выбора радио-кнопок
