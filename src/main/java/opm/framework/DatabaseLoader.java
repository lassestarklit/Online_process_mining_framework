package opm.framework;

import opm.framework.model.Employee;
import opm.framework.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;


    @Autowired
    public DatabaseLoader(EmployeeRepository repository) {
        this.employeeRepository = repository;

    }

    @Override
    public void run(String... strings) throws Exception {
        this.employeeRepository.save(new Employee("Lasse", "Starklit", "lasse@starklit.dk"));
        this.employeeRepository.save(new Employee("John", "Snow", "john@snow.dk"));


    }
}