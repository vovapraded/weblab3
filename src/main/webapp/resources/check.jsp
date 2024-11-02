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

  <tbody>
  <tr>


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
      <form id="homeForm" method="GET">
        <button type="submit">Go to Home</button>
      </form>
    </td>
  </tr>

  <tr>

  </tr>
  </tbody>
</table>
<script>
  // Прямо вставляем значение атрибута result в JS код
  var result = <%= request.getAttribute("result") %>;
  console.log("Result from server:", result);
</script>
<script type="module" src="/views/check.js"></script>


</body>
</html>