package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Post;

public interface IPostRepository extends JpaRepository<Post, UUID> {
	@Query("""
			SELECT p FROM Post p
			WHERE p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PUBLIC
			   OR (p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PRIVATE
			       AND p.author.id = :userID)
			   OR (p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.FRIENDS
			       AND p.author.id = :userID)
			   OR (p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.FRIENDS
			       AND EXISTS (
			           SELECT 1 FROM Connection c
			           WHERE c.status = com.socialnetwork.socialnetwork.enums.ConnectionStatus.ACCEPTED
			             AND ((c.requester.id = p.author.id AND c.receiver.id = :userID)
			               OR (c.receiver.id = p.author.id AND c.requester.id = :userID))
			       ))
			""")
	List<Post> findAllPostOfUser(@Param("userID") UUID userID);

	@Query("SELECT p FROM Post p WHERE p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PUBLIC")
	List<Post> findByVisibilityPublic();

}
