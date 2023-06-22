package com.uu.au.repository;

import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.Achievement;
import com.uu.au.models.AchievementPushedBack;
import com.uu.au.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface AchievementPushedBackRepository
        extends JpaRepository<AchievementPushedBack, Long> {

    Set<AchievementPushedBack> findAllByEnrolmentId(Long id);
    Set<AchievementPushedBack> findAllByAchievementId(Long achievementId);

    default Set<Achievement> findAllActivePushBacksForId(User user) {
        return findAllByEnrolmentId(user.currentEnrolment().orElseThrow(UserErrors::enrolmentNotFound).getId())
                .stream()
                .filter(AchievementPushedBack::isActive)
                .map(AchievementPushedBack::getAchievement)
                .collect(Collectors.toSet());
    }
}


