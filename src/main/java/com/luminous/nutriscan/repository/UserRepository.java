package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByWechatOpenId(String wechatOpenId);
    Optional<User> findByUserUid(String userUid);
}
