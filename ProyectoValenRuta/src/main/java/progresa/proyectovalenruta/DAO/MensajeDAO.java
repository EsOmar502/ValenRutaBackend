package progresa.proyectovalenruta.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import progresa.proyectovalenruta.Entity.Mensaje;

import java.util.List;

public interface MensajeDAO extends JpaRepository<Mensaje, Long> {

    // All messages for a user (sent or received), ordered by date
    List<Mensaje> findByEmisor_EmailOrReceptor_EmailOrderByFechaAsc(
            String emisorEmail,
            String receptorEmail
    );

    // Messages between two specific users, ordered chronologically
    @Query("""
        SELECT m FROM Mensaje m
        WHERE (m.emisor.id = :userId AND m.receptor.id = :otherUserId)
           OR (m.emisor.id = :otherUserId AND m.receptor.id = :userId)
        ORDER BY m.fecha ASC
    """)
    List<Mensaje> findConversacion(
            @Param("userId") Long userId,
            @Param("otherUserId") Long otherUserId
    );

    // Count unread messages from a specific sender to a specific receiver
    @Query("""
        SELECT COUNT(m) FROM Mensaje m
        WHERE m.emisor.id = :senderId
          AND m.receptor.id = :receiverId
          AND m.leido = false
    """)
    int countNoLeidos(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );

    // Mark all messages from sender to receiver as read
    @Modifying
    @Query("""
        UPDATE Mensaje m SET m.leido = true
        WHERE m.emisor.id = :senderId
          AND m.receptor.id = :receiverId
          AND m.leido = false
    """)
    void marcarComoLeidos(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );

    // Total unread for a user
    int countByReceptor_IdAndLeidoFalse(Long receptorId);
}
