package com.covid.covid.repository;
import com.covid.covid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface Userrepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByUsernameIgnoreCase(String username);
}
