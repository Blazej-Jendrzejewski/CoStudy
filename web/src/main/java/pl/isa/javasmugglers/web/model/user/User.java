package pl.isa.javasmugglers.web.model.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pl.isa.javasmugglers.web.model.Course;
import pl.isa.javasmugglers.web.model.CourseRegistration;
import pl.isa.javasmugglers.web.model.ExamResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity(name = "users")
@Table(
        name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "user_email_unique", columnNames = "email")
}
)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class User {



    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    @Column(
            updatable = false
    )
    private Long id;

    @Column(
            name = "email",
            nullable = false
    )
    private String email;

    public enum userType {STUDENT, PROFESSOR, ADMIN}
    @Enumerated(EnumType.STRING)
    @Column(
            columnDefinition = "enum('STUDENT', 'PROFESSOR', 'ADMIN')"
    )
    private UserType type;




    @Column(
            nullable = false
    )
    private String password;

    @Column(
            nullable = false
    )
    private String firstName;

    @Column(
            nullable = false
    )
    private String lastName;

    public enum accountStatus {ACTIVE, PENDING, REJECTED}
    @Enumerated(EnumType.STRING)
    @Column(
            columnDefinition = "enum('ACTIVE', 'PENDING', 'REJECTED')"
    )
    private accountStatus status;

    //relacje do innych tabeli
    @OneToMany(mappedBy = "professorId")
    private List<Course> courses;

    @OneToMany(mappedBy = "studentId")
    private List<CourseRegistration> courseRegistrationsList;

    @OneToMany(mappedBy = "studentId")
    private List<ExamResult> examResultList;

    public User(String email,
                String password,
                String firstName,
                String lastName,
                UserType type
                )
    {
        this.email = email;
        this.type = type;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }




    public String getPassword() {
        return password;
    }









}
