package com.team.socialnetwork.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.team.socialnetwork.entity.Notification;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.NotificationRepository;
import com.team.socialnetwork.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                messagingTemplate,
                userRepository
        );
    }

    @Test
    @DisplayName("Should create NotificationService successfully")
    void shouldCreateNotificationServiceSuccessfully() {
        assertNotNull(notificationService);
    }

    @Test
    @DisplayName("Should handle basic notification creation")
    void shouldHandleBasicNotificationCreation() {
        User recipient = new User();
        recipient.setId(1L);
        recipient.setUsername("recipient");

        User actor = new User();
        actor.setId(2L);
        actor.setUsername("actor");

        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Esta es una prueba básica - solo verificamos que el constructor funciona
        // y que podemos llamar al servicio sin errores
        notificationService.createAndSendNotification(recipient, actor, Notification.NotificationType.FOLLOW);

        verify(notificationRepository).save(any(Notification.class));
    }
}