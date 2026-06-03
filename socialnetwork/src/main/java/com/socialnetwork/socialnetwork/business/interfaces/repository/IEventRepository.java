package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.enums.VisibilityType;


public interface IEventRepository extends JpaRepository<Event, UUID> {

	@Query("""
			SELECT e FROM Event e WHERE e.eventDate > :currentdate ORDER BY e.eventDate ASC
			""")
	public Optional<List<Event>> getEventByDate(@Param("currentdate") LocalDateTime currentdate);
	
	public Optional<List<Event>> findByVisibilityType(VisibilityType visibilityType);
	
	@Query("""
			SELECT e FROM Event e
			WHERE (e.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PUBLIC
			       AND e.eventDate >= :eventDate)
			   OR (e.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PRIVATE
			       AND e.creator.id = :userID AND e.eventDate >= :eventDate)
			   OR (e.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.FRIENDS
			       AND e.creator.id = :userID AND e.eventDate >= :eventDate)
			   OR (e.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.FRIENDS
			       AND e.eventDate >= :eventDate
			       AND EXISTS (
			           SELECT 1 FROM Connection c
			           WHERE c.status = com.socialnetwork.socialnetwork.enums.ConnectionStatus.ACCEPTED
			             AND ((c.requester.id = e.creator.id AND c.receiver.id = :userID)
			               OR (c.receiver.id = e.creator.id AND c.requester.id = :userID))
			       ))
			""")
	Optional<List<Event>> findAllEventOfUser(@Param("userID") UUID userID, @Param("eventDate") LocalDateTime eventDate);
}
