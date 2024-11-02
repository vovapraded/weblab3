import {drawFullPlot, clearPlot} from './plot/plot.js';
// import {initForm} from "./form/form.js";
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




