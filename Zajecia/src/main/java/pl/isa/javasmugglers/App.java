package pl.isa.javasmugglers;


public class App 
{
    public static void main( String[] args ){
        //testowe odpalenie aplikacji
        MainMenu mainMenu = new MainMenu();
        System.out.println(mainMenu.getMenu());
        mainMenu.userSelection(mainMenu.getValidInput());
    }
}
