package use_case.login;

import data_access.InMemoryUserDataAccessObject;
import entity.CommonUserFactory;
import entity.User;
import entity.UserFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoginInteractorTest {

    private InMemoryUserDataAccessObject userRepository;
    private LoginInteractor interactor;
    private LoginOutputBoundary presenter;

    @Before
    public void setUp() {
        // **Setup Presenter**
        presenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(LoginOutputData outputData) {
                // No action needed for some tests
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        // **Setup In-Memory DAO and Interactor**
        userRepository = new InMemoryUserDataAccessObject();
        interactor = new LoginInteractor(userRepository, presenter);
    }

    @Test
    public void successTest() {
        // **Add User to DAO**
        UserFactory factory = new CommonUserFactory();
        User user = factory.create("Paul", "password");
        userRepository.save(user);

        // **Setup Success Presenter for this test**
        LoginOutputBoundary successPresenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(LoginOutputData outputData) {
                assertEquals("Paul", outputData.getUsername());
            }

            @Override
            public void prepareFailView(String error) {
                fail("Use case failure is unexpected.");
            }
        };

        // **Create Interactor with Success Presenter**
        LoginInteractor successInteractor = new LoginInteractor(userRepository, successPresenter);
        LoginInputData inputData = new LoginInputData("Paul", "password");

        // **Execute Use Case**
        successInteractor.execute(inputData);
    }

    @Test
    public void failurePasswordMismatchTest() {
        // **Add User to DAO with correct password**
        UserFactory factory = new CommonUserFactory();
        User user = factory.create("Paul", "password");
        userRepository.save(user);

        // **Setup Failure Presenter**
        LoginOutputBoundary failurePresenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(LoginOutputData outputData) {
                // this should never be reached since the test case should fail
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Incorrect password for \"Paul\".", error);
            }
        };

        // **Create Interactor with Failure Presenter**
        LoginInteractor failureInteractor = new LoginInteractor(userRepository, failurePresenter);
        LoginInputData inputData = new LoginInputData("Paul", "wrong");

        // **Execute Use Case**
        failureInteractor.execute(inputData);
    }

    @Test
    public void failureUserDoesNotExistTest() {
        // **Setup Failure Presenter**
        LoginOutputBoundary failurePresenter = new LoginOutputBoundary() {
            @Override
            public void prepareSuccessView(LoginOutputData outputData) {
                // this should never be reached since the test case should fail
                fail("Use case success is unexpected.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Paul: Account does not exist.", error);
            }
        };

        // **Create Interactor with Failure Presenter**
        LoginInteractor failureInteractor = new LoginInteractor(userRepository, failurePresenter);
        LoginInputData inputData = new LoginInputData("Paul", "password");

        // **Execute Use Case without adding user to DAO**
        failureInteractor.execute(inputData);
    }

    /**
     * Task 2.2: Test that the currentUser is set correctly upon successful login.
     */
    @Test
    public void successUserLoggedInTest() {
        // **Add User to DAO**
        UserFactory factory = new CommonUserFactory();
        User user = factory.create("Paul", "password123");
        userRepository.save(user);

        // **Assert No User is Logged In Initially**
        assertNull("No user should be logged in initially.", userRepository.getCurrentUser());

        // **Execute Login Use Case**
        LoginInputData inputData = new LoginInputData("Paul", "password123");
        interactor.execute(inputData);

        // **Assert User is Logged In After Successful Login**
        assertEquals("Current user should be 'Paul' after login.", "Paul", userRepository.getCurrentUser());
    }
}
