package com.project.interviewai.repository;

import com.project.interviewai.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    List<UserSkill> findByUserIdOrderByProficiencyDesc(Long userId);

    Optional<UserSkill> findByUserIdAndSkillName(Long userId, String skillName);
}
