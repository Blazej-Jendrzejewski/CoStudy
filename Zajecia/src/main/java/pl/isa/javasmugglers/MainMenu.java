package pl.isa.javasmugglers;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MainMenu {

    // Metoda wyświetlająca menu w konsoli
    public String getMenu() {
        return "WITAMY W MENAGERZE ZAJĘĆ \n" +
                "\nCo chcesz zrobić?: \n" +
                "1. Stworzyć konto użytkownika. \n" +
                "2. Zalogować się na konto ucznia. \n" +
                "3. Zalogować się na konto profesora." +
                "\nProszę podać odpowiedni numer: ";
    }

    // Metoda sprawdzająca input w menu głównym
    public int getValidInput() {
        Scanner scanner = new Scanner(System.in);
        int userInput = 0;
        boolean isValid = false;

        while (!isValid) {
            try {
                userInput = scanner.nextInt();
                if (userInput >= 1 && userInput <= 3) {
                    isValid = true;
                } else {
                    System.out.println("Nieprawidłowa liczba. Podaj liczbę od 1 do 3.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Nieprawidłowe dane wejściowe. Oczekiwano liczby.");
                scanner.nextLine();
            }
        }

        return userInput;
    }

    public String loginUserP() {
        String string2 = UserLogin.LoginProfessor.loginProfessor().toString();
        File userFile2 = new File("Zajecia/src/main/Resources/UserFiles/" + string2 + ".json");
        return userFile2.toString();
    }

    public String loginUserU() {
        String string1 = UserLogin.LoginStudent.loginStudent().toString();
        File userFile1 = new File("Zajecia/src/main/Resources/UserFiles/" + string1 + ".json");
        return userFile1.toString();
    }

    //metoda odpalająca odpowiednie klasy w zależności od inputu użytkownika
    public void userSelection(int userSelection) {
        switch (userSelection) {
            case 1: {
                //Tutaj wywołuję swoją metodę Błażej
                MenuAddUser newUser = new MenuAddUser();
                newUser.addUser();
                break;
            }
            case 2: {

                System.out.print("Wybrano 2");
                final String uczenPathName = loginUserU();
                break;
            }
            case 3: {

                System.out.print("Wybrano 3");
                final String profesorPathName = loginUserP();
                break;
            }
            case 4:
                //Możliwość wyjścia z programu
                System.out.println("Dziękujemy. Trwa wyłączanie programu....");
                System.exit(0);
                break;
        }
    }

}
