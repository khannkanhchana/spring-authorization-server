//package co.istad.kanhchana.authserver.user.entity;
//
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "app_users")
//public class UserEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String username;
//
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    public UserEntity() {
//    }
//
//    public UserEntity(String username, String email) {
//        this.username = username;
//        this.email = email;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//}