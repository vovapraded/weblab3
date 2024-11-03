
import {initInteraction} from "./interaction/interaction.js"

document.addEventListener('DOMContentLoaded', () => {
    initialize();
});
// Инициализация
function initialize() {
    window.updatePlot()
    initInteraction();
    document.getElementById("tableUpdater").click();
}




