<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>web</title>
    <link rel="stylesheet" href="/views/styles.css"> <!-- Подключение внешнего CSS файла -->
</head>
<body>
<table>
    <tr>
        <th>ФИО</th>
        <th>Номер группы</th>
        <th>Номер варианта</th>
    </tr>
    <tr>
        <th>Прадед Владимир Владимирович</th>
        <th>P3206</th>
        <th>115225</th>
    </tr>
    <tbody>
    <tr>
        <td>
            <svg id="mySvg" width="400" height="400" viewBox="0 0 200 200" class="svg">
                <g class="grid">
                    <line x1="100" y1="0" x2="100" y2="200"/>
                    <line x1="0" y1="100" x2="200" y2="100"/>
                </g>
            </svg>

        </td>
        <td>
            <form id="myForm">
                <label for="xInput">X:</label>
                <input id="xInput" type="number" min="-3" max="5" step="1" value="0" name="x" required/>
                <label for="yInput">Y:</label>
                <input type="text" id="yInput" name="y" required/>
                <fieldset>
                <legend>Выберите R:</legend>
                    <label>
                        <input type="radio" name="R" value="1" checked>
                        1
                    </label>
                    <label>
                        <input type="radio" name="R" value="2">
                        2
                    </label>
                    <label>
                        <input type="radio" name="R" value="3">
                        3
                    </label>
                    <label>
                        <input type="radio" name="R" value="4">
                        4
                    </label>
                    <label>
                        <input type="radio" name="R" value="5">
                        5
                    </label>
                </fieldset>
                <button type="submit">Submit</button>
                <button type="button" id="resetSessionBtn">Reset Session Data</button>
            </form>
            <p id="error-message" style="color: red;"></p>
            <div id="result"></div>
       </td>
    <td>


        <table id="dataTable">
            <thead>
            <tr>
                <th>x</th>
                <th>y</th>
                <th>R</th>
                <th>result</th>
            </tr>
            </thead>
            <tbody>
            <!-- Здесь будут появляться строки -->
            </tbody>
        </table>
    </td>
    </tr>
    </tbody>
</table>

<script type="module" src="/views/home.js"></script>


</body>
</html>