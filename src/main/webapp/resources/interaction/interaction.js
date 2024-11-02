import { getSelectedR} from "../plot/plot.js";

// Получаем элемент SVG по его ID
const svg = document.getElementById('mySvg');
const multiplier = 75;
const R = getSelectedR();
export function initInteraction() {
    // Добавляем обработчик клика по SVG
    svg.addEventListener('click', function (event) {

        // Создаём SVGPoint для хранения координат клика
        const point = svg.createSVGPoint();

        // Устанавливаем экранные координаты клика
        point.x = event.clientX;
        point.y = event.clientY;

        // Преобразуем экранные координаты в SVG
        const svgPoint = point.matrixTransform(svg.getScreenCTM().inverse());

        // Получаем размеры и положение SVG
        const viewBox = svg.viewBox.baseVal;

        // Координаты относительно центра графика
        const x = ((svgPoint.x - viewBox.width / 2) / multiplier/R).toFixed(2);  // Масштабируем координаты X
        const y = (-(svgPoint.y - viewBox.height / 2) / multiplier/R).toFixed(2);  // Масштабируем координаты Y


        // Создаём точку на графике
        pointSaveCommand([{name:'x', value:x}, {name:'y', value:y}, {name:'r', value:R}]);
        // createPoint(x, y, "point",R);

        // Формируем строку запроса с координатами
        const queryString = `x=${x}&y=${y}&R=${R}`;
        console.log("x"+x);
        console.log("y"+y);

// Пример для изменения URL без перезагрузки страницы

// Изменение URL без перезагрузки страницы
    });
}