package dao;

import models.User;

import java.util.List;

public interface UserDao {

    void save(User user);

    void update(User user);

    void delete(User user);

    User findById (int id);

    User findByName(String name);

    List<User> findAll();

    boolean authUser(String userName, String password);

}
