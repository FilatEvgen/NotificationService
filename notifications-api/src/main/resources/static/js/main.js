const notificationCountElement = document.getElementById("notificationCountMain");
let notificationCount = 0;

// Устанавливаем WebSocket соединение
const socket = new WebSocket("ws://localhost:8081/notifications/ws");

socket.onopen = function() {
    console.log("WebSocket соединение установлено.");
    // Подписываемся на уведомления
    socket.send("subscribe:Notifications_Channel");
};

socket.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log("Получено сообщение:", message);

    // Обновляем счетчик уведомлений
    notificationCount++;
    notificationCountElement.textContent = `(${notificationCount})`;

    // Показываем всплывающее уведомление
    showNotification(message);
};

function showNotification(message) {
    const notificationArea = document.getElementById("notificationArea");
    const notification = document.createElement("div");
    notification.className = "notification";
    notification.textContent = message.title + ": " + message.message;

    // Стили для уведомления
    notification.style.border = "1px solid #007BFF"; // Синий цвет рамки
    notification.style.padding = "15px";
    notification.style.marginBottom = "10px";
    notification.style.borderRadius = "5px";
    notification.style.backgroundColor = "#e7f3ff"; // Светло-синий фон
    notification.style.color = "#0056b3"; // Темно-синий текст
    notification.style.transition = "opacity 0.5s, transform 0.5s"; // Плавное исчезновение и анимация
    notification.style.opacity = "0";
    notification.style.transform = "translateY(-20px)"; // Начальная позиция для анимации

    // Добавляем уведомление в контейнер
    notificationArea.appendChild(notification);

    // Анимация появления
    setTimeout(() => {
        notification.style.opacity = "1"; // Плавное появление
        notification.style.transform = "translateY(0)"; // Возвращаем на место
    }, 10); // Небольшая задержка для запуска анимации

    // Удаляем уведомление через 5 секунд
    setTimeout(() => {
        notification.style.opacity = "0"; // Плавное исчезновение
        setTimeout(() => {
            notification.remove(); // Удаляем элемент из DOM
        }, 500); // Время, соответствующее времени исчезновения
    }, 5000);
}