package com.bscm.repository;

import com.bscm.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /** 根据手机号查找用户 */
  Optional<User> findByPhone(String phone);

  /** 根据用户名查找用户 */
  Optional<User> findByUsername(String username);

  /** 检查手机号是否存在 */
  boolean existsByPhone(String phone);

  /** 检查用户名是否存在 */
  boolean existsByUsername(String username);
}
