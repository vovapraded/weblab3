// Функция для обновления текущей даты и времени

function updateClock() {
    const now = new Date();
    const date = now.toLocaleDateString('ru-RU'); // форматирует дату
    const time = now.toLocaleTimeString('ru-RU'); // форматирует время

    // Отображаем дату и время в элементе с id="clock"
    document.getElementById('clock').textContent = `${date} ${time}`;
}

    // Обновляем часы один раз при загрузке страницы
    updateClock();
// Запускаем обновление каждые 8 секунд (8000 миллисекунд)
    setInterval(updateClock, 8000);


