
package com.sbs.sbsgroup7.DataSource;

import com.sbs.sbsgroup7.model.Appointment;
import com.sbs.sbsgroup7.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByUser(User user);

}
