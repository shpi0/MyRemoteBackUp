package services;

import dao.UserDao;
import dao.UserDaoImpl;
import models.User;

public class UserService {

    private UserDao userDao = new UserDaoImpl();

    public UserService() {
    }

    public void register(User user) {
        userDao.save(user);
    }

    public User findByName(String name) {
        return userDao.findByName(name);
    }

    public boolean authUser(String user, String pass) {
        return userDao.authUser(user, pass);
    }

}
