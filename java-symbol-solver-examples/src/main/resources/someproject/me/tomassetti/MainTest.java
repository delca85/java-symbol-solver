package someproject.me.tomassetti;

import me.tomassetti.Address;
import me.tomassetti.Agenda;
import me.tomassetti.Person;

import java.util.List;

public class MainTest{
    public static void main(String[] args){
        Agenda agenda = new Agenda();
        Person bibi = new Person();
        Address add1 = new Address();
        add1.setCity("Milano");
        add1.setNumber(35);
        add1.setStreet("Viale Umbria");
        add1.setZipCode(20135);
        agenda.addPerson(bibi);
        List<Address> steAddress = agenda.findAddressesOfPersons("ste");
        if (steAddress.isEmpty()){
            Person ste = new Person();
            Address add2 = new Address();
            add2.setCity("Siegen");
            add2.setNumber(2);
            add2.setZipCode(12345);
            add2.setStreet("via crucca");
        }
        else
            steAddress.get(0).toString();
    }
}