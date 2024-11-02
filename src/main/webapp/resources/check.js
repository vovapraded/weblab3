import {displaySessionData, initTable} from './table/table.js';
document.addEventListener('DOMContentLoaded', () => {
    onLoading();
    initTable();
    initBack();

});
function onLoading(){
    const params = new URLSearchParams(window.location.search);

    const xValue = params.get('x');
    const yValue = params.get('y');
    const rValue = params.get('R');
    const resultValue = result; // Результат вычислений с сервера
    console.log("res "+resultValue)
// Получаем текущие данные из sessionStorage или создаем новый массив
    let sessionData = JSON.parse(sessionStorage.getItem('formData')) || [];

// Добавляем новую запись в массив
    sessionData.unshift({ x: xValue, y: yValue, R: rValue, result: resultValue });

// Ограничиваем количество записей до 10
    if (sessionData.length > 10) {
        sessionData = sessionData.slice(0, 10);
    }

// Сохраняем обновленные данные в sessionStorage
    sessionStorage.setItem('formData', JSON.stringify(sessionData));
}
function initBack(){
    const form = document.getElementById('homeForm');
    form.addEventListener('submit', function(event) {
        // Останавливаем стандартное поведение формы (перезагрузка страницы)
        event.preventDefault();

        history.back();  // Имитируем нажатие на кнопку "Назад"

    });
}

