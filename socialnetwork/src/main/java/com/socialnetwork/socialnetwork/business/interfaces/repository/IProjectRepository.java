package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

public interface IProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find all projects created by a user
     */
    Optional<List<Project>> findByCreator(User creator);

    /**
     * Find all public projects
     */
    Optional<List<Project>> findByVisibilityType(VisibilityType visibilityType);

    /**
     * Find projects visible to a user (PUBLIC, PRIVATE owned by user, or FRIENDS if connected)
     */
    @Query("""
            SELECT p FROM Project p
            WHERE p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PUBLIC
               OR (p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.PRIVATE
                   AND p.creator.id = :userId)
               OR (p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.FRIENDS
                   AND p.creator.id = :userId)
               OR (p.visibilityType = com.socialnetwork.socialnetwork.enums.VisibilityType.FRIENDS
                   AND EXISTS (
                       SELECT 1 FROM ProjectMember pm
                       WHERE pm.project = p AND pm.user.id = :userId
                   ))
            """)
    Optional<List<Project>> findProjectsVisibleToUser(@Param("userId") UUID userId);
}
