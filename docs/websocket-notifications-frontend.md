# 🔔 Implementación de WebSocket para Notificaciones en el Frontend

## 📋 Índice
1. [Configuración inicial](#configuración-inicial)
2. [Conexión WebSocket](#conexión-websocket)
3. [Suscripción a notificaciones](#suscripción-a-notificaciones)
4. [Manejo de mensajes](#manejo-de-mensajes)
5. [Ejemplos prácticos](#ejemplos-prácticos)
6. [Estados y reconexión](#estados-y-reconexión)
7. [Integración con React/Vue/Angular](#integración-con-frameworks)

---

## 🚀 Configuración inicial

### Instalar dependencias

**Para JavaScript vanilla o React:**
```bash
npm install sockjs-client @stomp/stompjs
```

**Para Vue.js:**
```bash
npm install sockjs-client @stomp/stompjs
# O si usas Vue 3
npm install @vue/composition-api
```

**Para Angular:**
```bash
npm install sockjs-client @stomp/stompjs
npm install --save-dev @types/sockjs-client
```

---

## 🌐 Conexión WebSocket

### 1. Configuración base del cliente

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class NotificationWebSocket {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = [];
    this.reconnectDelay = 3000;
    this.maxReconnectAttempts = 5;
    this.reconnectAttempts = 0;
  }

  // Conectar al WebSocket
  connect(token) {
    return new Promise((resolve, reject) => {
      // Crear conexión SockJS
      const socket = new SockJS('http://localhost:8080/ws', null, {
        transports: ['websocket', 'xhr-polling']
      });

      // Configurar cliente STOMP
      this.client = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => {
          console.log('[WebSocket Debug]:', str);
        },
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      // Callback de conexión exitosa
      this.client.onConnect = (frame) => {
        console.log('🟢 WebSocket conectado:', frame);
        this.connected = true;
        this.reconnectAttempts = 0;
        resolve(frame);
      };

      // Callback de error de conexión
      this.client.onStompError = (frame) => {
        console.error('🔴 Error WebSocket:', frame.headers['message']);
        console.error('Detalles:', frame.body);
        this.connected = false;
        reject(new Error(frame.headers['message']));
      };

      // Callback de desconexión
      this.client.onDisconnect = () => {
        console.log('🟡 WebSocket desconectado');
        this.connected = false;
        this.handleReconnection();
      };

      // Iniciar conexión
      this.client.activate();
    });
  }

  // Desconectar
  disconnect() {
    if (this.client && this.connected) {
      this.client.deactivate();
      this.connected = false;
      console.log('🔴 WebSocket desconectado manualmente');
    }
  }

  // Manejo de reconexión automática
  handleReconnection() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`🔄 Reintentando conexión... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        if (!this.connected) {
          this.client.activate();
        }
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('🚫 Máximo de reintentos alcanzado. Conexión perdida permanentemente.');
    }
  }
}
```

---

## 🎯 Suscripción a notificaciones

### 2. Suscribirse a notificaciones del usuario

```javascript
class NotificationService extends NotificationWebSocket {
  constructor() {
    super();
    this.userId = null;
    this.onNotificationReceived = null;
    this.onUnreadCountChanged = null;
  }

  // Inicializar notificaciones para un usuario
  async initNotifications(token, userId) {
    this.userId = userId;
    
    try {
      await this.connect(token);
      this.subscribeToNotifications();
      this.subscribeToUserNotifications();
    } catch (error) {
      console.error('Error inicializando notificaciones:', error);
      throw error;
    }
  }

  // Suscribirse al canal de notificaciones del usuario
  subscribeToUserNotifications() {
    if (!this.connected || !this.userId) {
      console.warn('No se puede suscribir: WebSocket no conectado o userId no definido');
      return;
    }

    // Suscripción al canal personal de notificaciones
    const subscription = this.client.subscribe(
      `/topic/notifications/${this.userId}`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          console.log('🔔 Nueva notificación recibida:', data);
          
          // Callback para nueva notificación
          if (this.onNotificationReceived) {
            this.onNotificationReceived(data.notification);
          }
          
          // Callback para actualizar contador
          if (this.onUnreadCountChanged) {
            this.onUnreadCountChanged(data.unreadCount);
          }
          
        } catch (error) {
          console.error('Error procesando notificación:', error);
        }
      },
      {
        id: `notifications-${this.userId}`
      }
    );

    this.subscriptions.push(subscription);
  }

  // Enviar solicitud para suscribirse (opcional)
  subscribeToNotifications() {
    if (!this.connected) return;

    this.client.publish({
      destination: '/app/notifications/subscribe',
      body: JSON.stringify({ action: 'subscribe' })
    });
  }

  // Marcar todas las notificaciones como leídas vía WebSocket
  markAllNotificationsAsRead() {
    if (!this.connected) return;

    this.client.publish({
      destination: '/app/notifications/mark-all-read',
      body: JSON.stringify({ action: 'mark-all-read' })
    });
  }
}
```

---

## 📨 Manejo de mensajes

### 3. Estructura de mensajes recibidos

```javascript
// Ejemplo de mensaje WebSocket recibido
const messageExample = {
  type: "notification",
  notification: {
    id: 123,
    type: "LIKE",           // LIKE, COMMENT, COMMENT_LIKE, FOLLOW, FOLLOW_REQUEST
    actor: {
      id: 456,
      username: "juan_perez",
      fullName: "Juan Pérez"
    },
    post: {               // Solo si es relacionado a un post
      id: 789,
      description: "Mi post increíble..."
    },
    comment: {            // Solo si es relacionado a un comentario
      id: 101,
      text: "Excelente comentario..."
    },
    createdAt: "2025-09-25T14:12:03Z",
    read: false
  },
  unreadCount: 5
};

// Función para procesar diferentes tipos de notificaciones
function processNotification(notification) {
  switch (notification.type) {
    case 'LIKE':
      return `${notification.actor.fullName} le gustó tu post`;
    
    case 'COMMENT':
      return `${notification.actor.fullName} comentó tu post`;
    
    case 'COMMENT_LIKE':
      return `${notification.actor.fullName} le gustó tu comentario`;
    
    case 'FOLLOW':
      return `${notification.actor.fullName} comenzó a seguirte`;
    
    case 'FOLLOW_REQUEST':
      return `${notification.actor.fullName} solicitó seguirte`;
    
    default:
      return 'Nueva notificación';
  }
}
```

---

## 🎨 Ejemplos prácticos

### 4. Implementación completa

```javascript
// notification-manager.js
class NotificationManager {
  constructor() {
    this.service = new NotificationService();
    this.notifications = [];
    this.unreadCount = 0;
  }

  // Inicializar sistema de notificaciones
  async init(token, userId) {
    try {
      await this.service.initNotifications(token, userId);
      
      // Configurar callbacks
      this.service.onNotificationReceived = (notification) => {
        this.handleNewNotification(notification);
      };
      
      this.service.onUnreadCountChanged = (count) => {
        this.updateUnreadCount(count);
      };
      
      console.log('✅ Sistema de notificaciones iniciado');
      
    } catch (error) {
      console.error('❌ Error iniciando notificaciones:', error);
    }
  }

  // Manejar nueva notificación
  handleNewNotification(notification) {
    // Agregar a la lista local
    this.notifications.unshift(notification);
    
    // Mostrar notificación visual
    this.showNotificationToast(notification);
    
    // Actualizar UI
    this.updateNotificationsList();
    
    // Reproducir sonido (opcional)
    this.playNotificationSound();
  }

  // Mostrar toast/popup de notificación
  showNotificationToast(notification) {
    const message = this.formatNotificationMessage(notification);
    
    // Ejemplo usando una librería de toast
    if (window.showToast) {
      window.showToast({
        title: 'Nueva notificación',
        message: message,
        type: 'info',
        duration: 5000,
        onclick: () => {
          this.navigateToNotification(notification);
        }
      });
    }
    
    // O crear manualmente
    this.createCustomToast(message, notification);
  }

  // Crear toast personalizado
  createCustomToast(message, notification) {
    const toast = document.createElement('div');
    toast.className = 'notification-toast';
    toast.innerHTML = `
      <div class="toast-content">
        <div class="toast-avatar">
          <img src="/api/users/${notification.actor.id}/avatar" alt="${notification.actor.username}">
        </div>
        <div class="toast-text">
          <strong>${notification.actor.fullName}</strong>
          <p>${message}</p>
        </div>
        <button class="toast-close">&times;</button>
      </div>
    `;
    
    // Agregar eventos
    toast.querySelector('.toast-close').onclick = () => {
      document.body.removeChild(toast);
    };
    
    toast.onclick = () => {
      this.navigateToNotification(notification);
      document.body.removeChild(toast);
    };
    
    document.body.appendChild(toast);
    
    // Auto-remove después de 5 segundos
    setTimeout(() => {
      if (document.body.contains(toast)) {
        document.body.removeChild(toast);
      }
    }, 5000);
  }

  // Formatear mensaje de notificación
  formatNotificationMessage(notification) {
    const messages = {
      'LIKE': 'le gustó tu post',
      'COMMENT': 'comentó tu post',
      'COMMENT_LIKE': 'le gustó tu comentario',
      'FOLLOW': 'comenzó a seguirte',
      'FOLLOW_REQUEST': 'solicitó seguirte'
    };
    
    return messages[notification.type] || 'interactuó contigo';
  }

  // Navegar según el tipo de notificación
  navigateToNotification(notification) {
    switch (notification.type) {
      case 'LIKE':
      case 'COMMENT':
        if (notification.post) {
          // Navegar al post
          window.location.href = `/posts/${notification.post.id}`;
        }
        break;
        
      case 'COMMENT_LIKE':
        if (notification.post) {
          // Navegar al post y destacar el comentario
          window.location.href = `/posts/${notification.post.id}#comment-${notification.comment.id}`;
        }
        break;
        
      case 'FOLLOW':
      case 'FOLLOW_REQUEST':
        // Navegar al perfil del usuario
        window.location.href = `/users/${notification.actor.username}`;
        break;
    }
  }

  // Actualizar contador de no leídas
  updateUnreadCount(count) {
    this.unreadCount = count;
    
    // Actualizar badge en la UI
    const badge = document.getElementById('notification-badge');
    if (badge) {
      badge.textContent = count;
      badge.style.display = count > 0 ? 'inline' : 'none';
    }
    
    // Actualizar título de la página
    document.title = count > 0 ? `(${count}) Mi App` : 'Mi App';
  }

  // Actualizar lista de notificaciones en UI
  updateNotificationsList() {
    const container = document.getElementById('notifications-list');
    if (!container) return;
    
    container.innerHTML = this.notifications
      .slice(0, 20) // Mostrar solo las últimas 20
      .map(notification => `
        <div class="notification-item ${notification.read ? 'read' : 'unread'}" 
             data-id="${notification.id}">
          <img src="/api/users/${notification.actor.id}/avatar" 
               alt="${notification.actor.username}" 
               class="notification-avatar">
          <div class="notification-content">
            <p><strong>${notification.actor.fullName}</strong> ${this.formatNotificationMessage(notification)}</p>
            <small>${this.formatDate(notification.createdAt)}</small>
          </div>
          <div class="notification-indicator ${notification.read ? 'read' : 'unread'}"></div>
        </div>
      `).join('');
  }

  // Formatear fecha
  formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return 'Hace un momento';
    if (diffInSeconds < 3600) return `Hace ${Math.floor(diffInSeconds / 60)} min`;
    if (diffInSeconds < 86400) return `Hace ${Math.floor(diffInSeconds / 3600)} h`;
    return date.toLocaleDateString();
  }

  // Marcar todas como leídas
  markAllAsRead() {
    this.service.markAllNotificationsAsRead();
  }

  // Reproducir sonido de notificación
  playNotificationSound() {
    if ('Audio' in window) {
      const audio = new Audio('/sounds/notification.mp3');
      audio.volume = 0.3;
      audio.play().catch(e => console.log('No se pudo reproducir el sonido:', e));
    }
  }

  // Desconectar al cerrar la aplicación
  disconnect() {
    this.service.disconnect();
  }
}

// Uso global
const notificationManager = new NotificationManager();

// Inicializar cuando el usuario se autentica
async function initializeApp(token, user) {
  await notificationManager.init(token, user.id);
}

// Limpiar al cerrar
window.addEventListener('beforeunload', () => {
  notificationManager.disconnect();
});
```

---

## 🔄 Estados y reconexión

### 5. Manejo de estados de conexión

```javascript
class ConnectionStateManager {
  constructor(notificationService) {
    this.service = notificationService;
    this.connectionStatus = 'disconnected'; // disconnected, connecting, connected, reconnecting
    this.statusCallbacks = [];
  }

  // Suscribirse a cambios de estado
  onStatusChange(callback) {
    this.statusCallbacks.push(callback);
  }

  // Notificar cambio de estado
  updateStatus(newStatus) {
    if (this.connectionStatus !== newStatus) {
      this.connectionStatus = newStatus;
      this.statusCallbacks.forEach(callback => callback(newStatus));
      this.updateConnectionUI(newStatus);
    }
  }

  // Actualizar UI según el estado
  updateConnectionUI(status) {
    const indicator = document.getElementById('connection-indicator');
    if (!indicator) return;

    const statusConfig = {
      'connected': { text: '🟢 Conectado', class: 'connected' },
      'connecting': { text: '🟡 Conectando...', class: 'connecting' },
      'reconnecting': { text: '🟠 Reconectando...', class: 'reconnecting' },
      'disconnected': { text: '🔴 Desconectado', class: 'disconnected' }
    };

    const config = statusConfig[status];
    indicator.textContent = config.text;
    indicator.className = `connection-status ${config.class}`;
  }
}
```

---

## ⚛️ Integración con Frameworks

### 6. React Hook personalizado

```jsx
// hooks/useNotifications.js
import { useState, useEffect, useCallback } from 'react';

export const useNotifications = (token, userId) => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [connectionStatus, setConnectionStatus] = useState('disconnected');
  const [notificationService, setNotificationService] = useState(null);

  useEffect(() => {
    if (!token || !userId) return;

    const service = new NotificationService();
    
    const initService = async () => {
      try {
        setConnectionStatus('connecting');
        await service.initNotifications(token, userId);
        setConnectionStatus('connected');
        
        service.onNotificationReceived = (notification) => {
          setNotifications(prev => [notification, ...prev]);
        };
        
        service.onUnreadCountChanged = (count) => {
          setUnreadCount(count);
        };
        
        setNotificationService(service);
      } catch (error) {
        setConnectionStatus('disconnected');
        console.error('Error iniciando notificaciones:', error);
      }
    };

    initService();

    return () => {
      service?.disconnect();
    };
  }, [token, userId]);

  const markAllAsRead = useCallback(() => {
    notificationService?.markAllNotificationsAsRead();
  }, [notificationService]);

  return {
    notifications,
    unreadCount,
    connectionStatus,
    markAllAsRead
  };
};

// Componente de ejemplo
import React from 'react';
import { useNotifications } from './hooks/useNotifications';

const NotificationComponent = ({ token, user }) => {
  const { notifications, unreadCount, connectionStatus, markAllAsRead } = useNotifications(token, user?.id);

  return (
    <div className="notification-panel">
      <div className="notification-header">
        <h3>Notificaciones {unreadCount > 0 && <span className="badge">{unreadCount}</span>}</h3>
        <div className={`status ${connectionStatus}`}>{connectionStatus}</div>
        {unreadCount > 0 && (
          <button onClick={markAllAsRead}>Marcar todas como leídas</button>
        )}
      </div>
      
      <div className="notification-list">
        {notifications.map(notification => (
          <div key={notification.id} className={`notification ${notification.read ? 'read' : 'unread'}`}>
            <img src={`/api/users/${notification.actor.id}/avatar`} alt="" />
            <div className="content">
              <p><strong>{notification.actor.fullName}</strong> {formatNotificationMessage(notification)}</p>
              <small>{formatDate(notification.createdAt)}</small>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
```

### 7. Vue.js Composable

```javascript
// composables/useNotifications.js
import { ref, onMounted, onUnmounted } from 'vue';

export function useNotifications(token, userId) {
  const notifications = ref([]);
  const unreadCount = ref(0);
  const connectionStatus = ref('disconnected');
  let notificationService = null;

  const initNotifications = async () => {
    if (!token.value || !userId.value) return;

    notificationService = new NotificationService();
    
    try {
      connectionStatus.value = 'connecting';
      await notificationService.initNotifications(token.value, userId.value);
      connectionStatus.value = 'connected';
      
      notificationService.onNotificationReceived = (notification) => {
        notifications.value.unshift(notification);
      };
      
      notificationService.onUnreadCountChanged = (count) => {
        unreadCount.value = count;
      };
      
    } catch (error) {
      connectionStatus.value = 'disconnected';
      console.error('Error iniciando notificaciones:', error);
    }
  };

  const markAllAsRead = () => {
    notificationService?.markAllNotificationsAsRead();
  };

  onMounted(() => {
    initNotifications();
  });

  onUnmounted(() => {
    notificationService?.disconnect();
  });

  return {
    notifications,
    unreadCount,
    connectionStatus,
    markAllAsRead
  };
}
```

---

## 🎨 CSS para notificaciones

### 8. Estilos básicos

```css
/* notification-styles.css */
.notification-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 16px;
  min-width: 300px;
  z-index: 1000;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.notification-toast:hover {
  transform: translateY(-2px);
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toast-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.toast-text {
  flex: 1;
}

.toast-text strong {
  font-weight: 600;
  color: #333;
}

.toast-text p {
  margin: 4px 0 0;
  color: #666;
  font-size: 14px;
}

.toast-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
}

.notification-badge {
  background: #ff4444;
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 12px;
  font-weight: bold;
  position: absolute;
  top: -8px;
  right: -8px;
}

.connection-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.connection-status.connected { background: #d4edda; color: #155724; }
.connection-status.connecting { background: #fff3cd; color: #856404; }
.connection-status.reconnecting { background: #f8d7da; color: #721c24; }
.connection-status.disconnected { background: #f8d7da; color: #721c24; }

.notification-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f8f9fa;
}

.notification-item.unread {
  background-color: #f0f8ff;
  border-left: 3px solid #007bff;
}

.notification-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
}

.notification-content {
  flex: 1;
}

.notification-indicator.unread {
  width: 8px;
  height: 8px;
  background: #007bff;
  border-radius: 50%;
}
```

---

## 🔧 Configuración de producción

### 9. Variables de entorno

```javascript
// config.js
const config = {
  development: {
    websocketUrl: 'http://localhost:8080/ws',
    apiUrl: 'http://localhost:8080'
  },
  production: {
    websocketUrl: 'https://tu-dominio.com/ws',
    apiUrl: 'https://tu-dominio.com'
  }
};

export default config[process.env.NODE_ENV || 'development'];
```

---

## 🚀 Inicialización completa

### 10. Archivo principal de inicialización

```javascript
// main.js
import config from './config.js';

class AppNotifications {
  constructor() {
    this.manager = new NotificationManager();
    this.initialized = false;
  }

  async init(authToken, user) {
    if (this.initialized) return;

    try {
      // Verificar soporte de WebSocket
      if (!window.WebSocket && !window.SockJS) {
        console.warn('WebSocket no soportado en este navegador');
        return;
      }

      // Pedir permisos para notificaciones del navegador
      if ('Notification' in window) {
        const permission = await Notification.requestPermission();
        if (permission === 'granted') {
          console.log('✅ Permisos de notificación concedidos');
        }
      }

      // Inicializar sistema de notificaciones
      await this.manager.init(authToken, user.id);
      this.initialized = true;

      console.log('🎉 Sistema de notificaciones completamente inicializado');
      
    } catch (error) {
      console.error('❌ Error inicializando notificaciones:', error);
    }
  }

  disconnect() {
    this.manager.disconnect();
    this.initialized = false;
  }
}

// Instancia global
window.appNotifications = new AppNotifications();

// Auto-inicialización si hay token
document.addEventListener('DOMContentLoaded', () => {
  const token = localStorage.getItem('authToken');
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  
  if (token && user.id) {
    window.appNotifications.init(token, user);
  }
});
```

---

## ✅ Checklist de implementación

- [ ] Instalar dependencias (`sockjs-client`, `@stomp/stompjs`)
- [ ] Configurar clase base `NotificationWebSocket`
- [ ] Implementar `NotificationService` con callbacks
- [ ] Crear `NotificationManager` para lógica de UI
- [ ] Agregar manejo de reconexión automática
- [ ] Implementar toast/popup de notificaciones
- [ ] Configurar estados de conexión
- [ ] Integrar con framework (React/Vue/Angular)
- [ ] Agregar estilos CSS
- [ ] Configurar variables de entorno
- [ ] Pedir permisos de notificación del navegador
- [ ] Probar reconexión automática
- [ ] Validar en producción

---

¡Con esta guía tendrás un sistema completo de notificaciones WebSocket funcionando en tu frontend! 🎉