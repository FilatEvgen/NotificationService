// notifications.js
const notificationsDiv = document.getElementById('notifications');
const notificationsList = document.getElementById('notificationsList');
const notificationCount = document.getElementById('notificationCount');
const markAsViewedButton = document.getElementById('markAsViewedButton');
const socket = new WebSocket('ws://localhost:8081/notifications/ws');
const viewedNotificationIds = [];

// Переключение на вкладку уведомлений
document.querySelector('.navbar div:nth-child(2)').onclick = function() {
    notificationsDiv.style.display = notificationsDiv.style.display === 'none' ? 'block' : 'none';
};

socket.onopen = function() {
    console.log('Подключено к WebSocket');
    socket.send('subscribe:Notifications_Channel');
};

socket.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    console.log("Получено уведомление:", notification);
    displayNotification(notification);
};

socket.onclose = function() {
    console.log('Соединение с WebSocket закрыто');
};

function displayNotification(notification) {
    const notificationElement = document.createElement('div');
    notificationElement.className = 'notification';
    notificationElement.innerHTML = `<strong>${notification.title}</strong><p>${notification.message}</p>`;
    notificationElement.dataset.id = notification.id;
    console.log("Добавлено уведомление с id:", notification.id);
    notificationsList.appendChild(notificationElement);
    updateNotificationCount();
}

function updateNotificationCount() {
    const count = notificationsList.children.length;
    notificationCount.textContent = `(${count})`;
    // Отправляем количество уведомлений на главную страницу
    localStorage.setItem('notificationCount', count);
}

markAsViewedButton.onclick = function() {
    const idsToRemove = [];
    for (let notification of notificationsList.children) {
        idsToRemove.push(notification.dataset.id);
        notificationsList.removeChild(notification);
    }
    viewedNotificationIds.push(...idsToRemove);
    updateNotificationCount();
    console.log("Отправка идентификаторов на сервер:", idsToRemove);

    // Отправляем подтверждение на сервер
    socket.send(JSON.stringify({ type: 'notifications_viewed', ids: idsToRemove }));
};