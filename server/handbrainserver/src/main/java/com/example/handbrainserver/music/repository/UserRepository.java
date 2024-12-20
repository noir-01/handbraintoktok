package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    List<User> findByPhoneNumberHashIn(List<String> phoneNumberHashes);
    Optional<User> findByPhoneNumberHash(String phoneNUmberHash);
    Boolean existsUserByPhoneNumberHash(String phoneNumberHash);
}
