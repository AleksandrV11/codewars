package org.example.three.papers;

import java.util.*;
import java.util.stream.Collectors;

public class Inspector {

    private Set<String> allowedNations = new HashSet<>();//ок громадянство
    private Set<String> bannedNations = new HashSet<>(); //nou громадянство

    private Set<String> requiredForForeigners = new HashSet<>(); // Для іноземців
    private Set<String> requiredForCitizens = new HashSet<>();// Для громадян Arstotzka
    private Set<String> requiredForWorkers = new HashSet<>();// Для робітників
    private Set<String> requiredForAllEntrantsVaccines = new HashSet<>();// обов'язкова вакцинація

    private Map<String, Set<String>> requiredVaccinationsMap = new HashMap<>();// обов щеплення громадян різних стран
    private Set<String> vaccinesNotRequiredForEntrants = new HashSet<>();// ЦІ щеплення НЕПОТРІБНІ ДЛЯ УСІХ

    private String wantedCriminal = null;
//"passport" — обов’язковий для всіх.
//
//"ID_card" — тільки для громадян Arstotzka, якщо бюлетень вимагає.
//
//            "access_permit" — для іноземців, якщо потрібен доступ.
//
//            "grant_of_asylum" — замість access_permit для іноземців.
//
//            "certificate_of_vaccination" — якщо потрібна вакцина.
//
//"work_pass" — для робітників-іноземців.
//
//"diplomatic_authorization" — для іноземців-дипломатів (як альтернатива access_permit).

    public void receiveBulletin(String bulletin) {
        System.out.println("???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
        System.out.println(bulletin);
        wantedCriminal = null;
        String[] bulletinLines = bulletin.split("\n");
        for (String bulletinFild : bulletinLines) {
            if (bulletinFild.startsWith("Allow citizens of ")) { //якщо + громадянство
                String value = bulletinFild.substring("Allow citizens of ".length());
                if (value.contains(", ")) {
                    String[] nations = value.split(", ");
                    for (String string : nations) {
                        allowedNations.add(string.trim());
                        bannedNations.remove(string.trim());
                    }
                } else {
                    allowedNations.add(value.trim());
                    bannedNations.remove(value.trim());
                }
            } else if (bulletinFild.startsWith("Deny citizens of ")) {  //якщо - громадянство
                String value = bulletinFild.substring("Deny citizens of ".length());
                if (value.contains(", ")) {
                    String[] nations = value.split(", ");
                    for (String string : nations) {
                        bannedNations.add(string.trim());
                    }
                } else {
                    bannedNations.add(value.trim());
                }
            } else if (bulletinFild.startsWith("Foreigners require ")) {  //Іноземцям потрібен дозвіл на в'їзд
                String document = bulletinFild.substring("Foreigners require ".length());
                requiredForForeigners.add(document.trim());
            } else if (bulletinFild.startsWith("Citizens of Arstotzka require ")) {  //Громадянам Арстоцька потрібне посвідчення особи
                String document = bulletinFild.substring("Citizens of Arstotzka require ".length());
                requiredForCitizens.add(document.trim());
            } else if (bulletinFild.startsWith("Workers require ")) { //Працівникам потрібен робоча перепустка
                String document = bulletinFild.substring("Workers require ".length());
                requiredForWorkers.add(document.trim());
            } else if (bulletinFild.startsWith("Entrants no longer require ") &&
                    (bulletinFild.endsWith("vaccination") || bulletinFild.endsWith("vaccinations"))) {
                String vaccine = bulletinFild.substring("Entrants no longer require ".length()).trim();
                vaccine = vaccine.replaceAll("(?i) vaccinations?$", "");
                if (vaccine.contains(", ")) {
                    for (String v : vaccine.split(", ")) {
                        requiredForAllEntrantsVaccines.remove(v.trim());
                    }
                } else {
                    requiredForAllEntrantsVaccines.remove(vaccine.trim());
                }
            } else if (bulletinFild.startsWith("Entrants require ") &&
                    (bulletinFild.endsWith("vaccination") || bulletinFild.endsWith("vaccinations"))) {
                String vaccine = bulletinFild.substring("Entrants require ".length()).trim();
                vaccine = vaccine.replaceAll("(?i) vaccinations?$", "");
                if (vaccine.contains(", ")) {
                    for (String v : vaccine.split(", ")) {
                        requiredForAllEntrantsVaccines.add(v.trim());
                    }
                } else {
                    requiredForAllEntrantsVaccines.add(vaccine.trim());
                }//Citizens of Antegria, Impor, Republia, Kolechia no longer require rubella vaccination
            } else if (bulletinFild.startsWith("Citizens of ") &&
                    (bulletinFild.endsWith("vaccination") || bulletinFild.endsWith("vaccinations"))) {//вакц стран
                boolean remove = bulletinFild.contains("no longer require");
                int indCountryStart = "Citizens of ".length();
                int indCountryEnd = bulletinFild.indexOf(remove ? " no longer require" : " require");
                String countriesPart = bulletinFild.substring(indCountryStart, indCountryEnd).trim();
                int indV = bulletinFild.indexOf("require ");
                String vaccinePart = bulletinFild.substring(indV + "require ".length())
                        .replace(" vaccination", "")
                        .replace(" vaccinations", "")
                        .trim();
                Set<String> vaccines = Arrays.stream(vaccinePart.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
                String[] countries = countriesPart.split(",\\s*");
                for (String c : countries) {
                    if (remove) {
                        if (requiredVaccinationsMap.containsKey(c)) {
                            requiredVaccinationsMap.get(c).removeAll(vaccines);
                        }
                    } else {
                        requiredVaccinationsMap.putIfAbsent(c, new HashSet<>());
                        requiredVaccinationsMap.get(c).addAll(vaccines);
                    }
                }
            } else if (bulletinFild.startsWith("Foreigners no longer require ") &&
                    (bulletinFild.endsWith("vaccination") || bulletinFild.endsWith("vaccinations"))) {//непотрібні вакціни
                String vaccine = bulletinFild.substring("Foreigners no longer require ".length())
                        .replace(" vaccination", "")
                        .replace(" vaccinations", "");
                if (vaccine.contains(", ")) {
                    String[] stringsVaccinas = vaccine.split(", ");
                    for (String s : stringsVaccinas) {
                        vaccinesNotRequiredForEntrants.add(s.trim());
                        requiredForForeigners.remove(s.trim() + " vaccination");
                    }
                } else {
                    vaccinesNotRequiredForEntrants.add(vaccine.trim());
                    requiredForForeigners.remove(vaccine.trim() + " vaccination");
                }
            } else if (bulletinFild.startsWith("Wanted by the State: ")) {//злочинець
                wantedCriminal = bulletinFild.substring("Wanted by the State: ".length());
            }
        }
        System.out.println(allowedNations);//+ країни
        System.out.println(bannedNations);//-країни
        System.out.println(requiredForForeigners);  //для іноземців
        System.out.println(requiredForCitizens);//для громадян
        System.out.println(requiredForWorkers);//Для робітників
        System.out.println(requiredForAllEntrantsVaccines);// обов'язкова вакцинація
        System.out.println(requiredVaccinationsMap);// обов щеплення громадян різних стран
        System.out.println(vaccinesNotRequiredForEntrants);//ЦІ щеплення НЕПОТРІБНІ ДЛЯ УСІХ
        System.out.println("розиск - " + wantedCriminal);// запрет особі
    }

    public String inspect(Map<String, String> person) {
        System.out.println("---------------------------");
        printPerson(person);
        System.out.println("---------------------------");
        if (wantedCriminal != null) {   //перевірка на розшук
            String name = extractName(person);
            if (name != null) {
                if (namesMatch(wantedCriminal, name)) {
                    return "Detainment: Entrant is a wanted criminal.";
                }
            }
        }
        if (hasIdMismatch(person)) {    //перевірка на валідність ід
            return "Detainment: ID number mismatch.";
        }
        if (!person.containsKey("passport")) {  //паспорта -
            return "Entry denied: missing required passport.";
        } else {//паспорт +
            if (complianceCheck(person) != null) {
                return complianceCheck(person);         //валідність доків
            }
            String passportValue = person.get("passport");
            String nation = null;
            List<String> filds = Arrays.asList(passportValue.split("\n"));//філди паспорта

            for (String fild : filds) {
                if (fild.contains("NATION: ")) {
                    nation = fild.substring("NATION: ".length()).trim();
                }
            }
            String expLine = getExpDateString("passport", person); // перевірка паспорта по дате
            if (expLine != null && isExpired(expLine)) return "Entry denied: passport expired.";
            if (bannedNations.contains(nation)) {  //нація в бані
                return "Entry denied: citizen of banned nation.";
            }
            if (!allowedNations.isEmpty() && !allowedNations.contains(nation)) {  //нація явно дозволене
                return "Entry denied: citizen of banned nation.";
            }
            if ("Arstotzka".equals(nation)) {  // перевір  громадянства Артроц ід без бюлетеня
                System.out.println("*********111************");
                if (!requiredForCitizens.isEmpty()) {
                    if (requiredForCitizens.contains("ID card")) {
                        if (!person.containsKey("ID_card")) {
                            return "Entry denied: missing required ID card.";
                        } else {
                            String lineId = person.get("ID_card");  //key "ID_card"
                            String expId = Arrays.asList(lineId).stream()
                                    .filter(a -> a.startsWith("EXP: "))
                                    .map(a -> a.substring("EXP: ".length()).trim())
                                    .findFirst()
                                    .orElse(null);// перевірка id по дате
                            if (expId != null && isExpired(expId)) return "Entry denied: ID_card expired.";
                        }
                    }
                }
                if (!requiredForAllEntrantsVaccines.isEmpty()) {
                    if (!person.containsKey("certificate_of_vaccination")) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    String cert = person.get("certificate_of_vaccination");
                    if (cert == null) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    // Перевіряємо EXP сертифіката
                    String expVacc = getExpDateString("certificate_of_vaccination", person);// перевірка  по дате
                    if (expVacc != null && isExpired(expVacc))
                        return "Entry denied: certificate of vaccination expired.";
                    // Перевіряємо конкретну вакцину
                    Set<String> vaccines = getVaccines("certificate_of_vaccination", person);
                    System.out.println(vaccines);
                    for (String required : requiredForAllEntrantsVaccines) {
                        boolean hasVaccine = vaccines.stream()
                                .anyMatch(v -> v.equalsIgnoreCase(required));
                        if (!hasVaccine) {
                            return "Entry denied: missing required vaccination.";
                        }
                    }
                }
                if (!requiredVaccinationsMap.isEmpty() && requiredVaccinationsMap.containsKey(nation)
                        && !requiredVaccinationsMap.get(nation).isEmpty()) {
                    Set<String> vaccinasIsMap = requiredVaccinationsMap.get(nation);
                    if (!person.containsKey("certificate_of_vaccination")) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    String cert = person.get("certificate_of_vaccination");
                    if (cert == null) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    // Перевіряємо EXP сертифіката
                    String expVacc = getExpDateString("certificate_of_vaccination", person);// перевірка  по дате
                    if (expVacc != null && isExpired(expVacc))
                        return "Entry denied: certificate of vaccination expired.";
                    Set<String> vaccinesIsPerson = getVaccines("certificate_of_vaccination", person);
                    for (String vaccina : vaccinasIsMap) {
                        boolean hasVaccine = vaccinesIsPerson.stream()
                                .anyMatch(v -> v.equalsIgnoreCase(vaccina));
                        if (!hasVaccine) {
                            return "Entry denied: missing required vaccination.";
                        }
                    }
                }
                return "Glory to Arstotzka.";// якщо попередній закоментований

            } else if (!"Arstotzka".equals(nation)) {
                System.out.println("*********222************");
                boolean conditionalReservation = false;
                if (person.containsKey("diplomatic_authorization")) {// якщо дипломат є прівелеї
                    String expDiplomat = getExpDateString("diplomatic_authorization", person);// перевірка діпломата  по дате
                    if (expDiplomat != null && isExpired(expDiplomat))
                        return "Entry denied: diplomatic authorization expired.";
                    List<String> listAccess = Arrays.stream(person.get("diplomatic_authorization").split("\n"))
                            .filter(a -> a.startsWith("ACCESS: "))
                            .map(a -> a.substring("ACCESS: ".length()).trim())
                            .flatMap(a -> Arrays.stream(a.split(",\\s+")))
                            .toList();
                    if (!listAccess.contains("Arstotzka")) {
                        return "Entry denied: invalid diplomatic authorization.";
                    }
                    conditionalReservation = true;
                }
                if (person.containsKey("grant_of_asylum")) {
                    String expAsylum = getExpDateString("grant_of_asylum", person);
                    if (expAsylum != null && isExpired(expAsylum)) {
                        return "Entry denied: grant of asylum expired.";
                    }
                    conditionalReservation = true;
                }
                if (person.containsKey("work_pass")) {  // якщо "work_pass" ключ персона Є
                    String expWorkPass = getExpDateString("work_pass", person);
                    if (expWorkPass != null && isExpired(expWorkPass))
                        return "Entry denied: work pass expired.";
                }
                if (requiredForWorkers.contains("work pass")) {     //типо работа
                    boolean isWorker = person.values().stream()
                            .flatMap(v -> Arrays.stream(v.split("\n")))
                            .anyMatch(line -> line.equals("PURPOSE: WORK"));
                    if (isWorker) {
                        if (!person.containsKey("work_pass")) {  // якщо "work_pass" ключа персона нема
                            return "Entry denied: missing required work pass.";
                        }
                        String expWorkPass = getExpDateString("work_pass", person);
                        if (expWorkPass != null && isExpired(expWorkPass))
                            return "Entry denied: work pass expired.";
                        conditionalReservation = true;
                    }
                }
                if (person.containsKey("access_permit")) { //перевірка на вілідність поля
                    // на "access_permit"
                    String expPermit = getExpDateString("access_permit", person);// по дате
                    if (expPermit != null && isExpired(expPermit))
                        return "Entry denied: access permit expired.";
                }
                if (!requiredForForeigners.isEmpty()) {
                    System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
                    System.out.println(" БРОНЬ " + conditionalReservation);
                    if (requiredForForeigners.contains("access permit") && !conditionalReservation) { //запит з бюлетня
                        // на "access_permit"
                        if (!person.containsKey("access_permit")) {  // якщо access_permit ключа персона нема
                            return "Entry denied: missing required access permit.";
                        }
                    }
                    for (String fild : requiredForForeigners) {
                        System.out.println("W" + fild + "W");
                        if (fild.trim().endsWith(" vaccination")) {
                            String vaccina = fild.replace(" vaccination", "").trim();
                            System.out.println("F" + vaccina + "F");
                            if (!person.containsKey("certificate_of_vaccination")) {
                                return "Entry denied: missing required certificate of vaccination.";
                            }
                            String cert = person.get("certificate_of_vaccination");
                            if (cert == null) {
                                return "Entry denied: missing required certificate of vaccination.";
                            }
                            // Перевіряємо EXP сертифіката
                            String expVacc = getExpDateString("certificate_of_vaccination", person);
                            if (expVacc != null && isExpired(expVacc))
                                return "Entry denied: certificate of vaccination expired.";
                            // Перевіряємо конкретну вакцину
                            Set<String> vaccines = getVaccines("certificate_of_vaccination", person);
                            System.out.println(vaccines);
                            boolean hasVaccine = vaccines.stream()
                                    .anyMatch(v -> v.equalsIgnoreCase(vaccina));
                            if (!hasVaccine) {
                                return "Entry denied: missing required vaccination.";
                            }
                        }
                    }
                }
                if (!requiredVaccinationsMap.isEmpty() && requiredVaccinationsMap.containsKey(nation)
                        && !requiredVaccinationsMap.get(nation).isEmpty()) {
                    System.out.println("GGGGGGGGGGGGGGGG");
                    Set<String> vaccinasIsMap = requiredVaccinationsMap.get(nation);
                    if (!person.containsKey("certificate_of_vaccination")) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    String cert = person.get("certificate_of_vaccination");
                    if (cert == null) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    // Перевіряємо EXP сертифіката
                    String expVacc = getExpDateString("certificate_of_vaccination", person);
                    if (expVacc != null && isExpired(expVacc))
                        return "Entry denied: certificate of vaccination expired.";
                    Set<String> vaccinesIsPerson = getVaccines("certificate_of_vaccination", person);
                    for (String vaccina : vaccinasIsMap) {
                        boolean hasVaccine = vaccinesIsPerson.stream()
                                .anyMatch(v -> v.equalsIgnoreCase(vaccina));
                        if (!hasVaccine) {
                            return "Entry denied: missing required vaccination.";
                        }
                    }
                }
                if (!requiredForAllEntrantsVaccines.isEmpty()) {
                    System.out.println("ttttttttttttttttttttttttttt");
                    if (!person.containsKey("certificate_of_vaccination")) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    String cert = person.get("certificate_of_vaccination");
                    if (cert == null) {
                        return "Entry denied: missing required certificate of vaccination.";
                    }
                    String expVacc = getExpDateString("certificate_of_vaccination", person);
                    if (expVacc != null && isExpired(expVacc)) {
                        return "Entry denied: certificate of vaccination expired.";
                    }
                    Set<String> entrantVaccines = getVaccines("certificate_of_vaccination", person);
                    for (String required : requiredForAllEntrantsVaccines) {
                        boolean hasVaccine = entrantVaccines.stream()
                                .anyMatch(v -> v.equalsIgnoreCase(required));
                        if (!hasVaccine) {
                            return "Entry denied: missing required vaccination.";
                        }
                    }
                }
                return "Cause no trouble.";
            }
            System.out.println();
        }
        return null;
    }

    public String extractName(Map<String, String> person) {
        for (String value : person.values()) {
            String[] lines = value.split("\n");
            for (String fild : lines) {
                if (fild.startsWith("NAME: ")) {
                    return fild.substring("NAME: ".length());
                }
            }
        }
        return null;
    }

    public void printPerson(Map<String, String> person) {
        for (Map.Entry<String, String> entry : person.entrySet()) {
            System.out.println("Fild : ");
            System.out.println(entry.getKey() + " -- " + person.values());
            System.out.println("..................................");

        }
    }

    public boolean hasIdMismatch(Map<String, String> person) {
        List<String> idList = person.values().stream()
                .flatMap(a -> Arrays.stream(a.split("\n")))
                .filter(a -> a.startsWith("ID"))
                .map(a -> a.substring("ID#: ".length()).trim())
                .toList();
        return idList.stream().distinct().count() > 1;
    }

    public boolean namesMatch(String bulletinCriminalName, String namePerson) {
        List<String> bulletinList = Arrays.stream(bulletinCriminalName.split("[,\\s]+")).toList();
        List<String> namePersonList = Arrays.stream(namePerson.split("[,\\s]+")).toList();
        if (bulletinList.size() != namePersonList.size()) return false;
        for (String word : bulletinList) {
            if (!namePersonList.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public String complianceCheck(Map<String, String> person) {
        Map<String, String> keyToMessage = Map.of(
                "NAME", "name",
                "ID#", "ID number",
                "NATION", "nationality",
                "DOB", "date of birth"
        );
        List<String> keysToCheck = List.of("NAME", "ID#", "NATION", "DOB");

        for (String key : keysToCheck) {
            Set<String> values = new HashSet<>();
            for (String doc : person.values()) {
                List<String> lines = Arrays.asList(doc.split("\n"));
                for (String line : lines) {
                    if (line.startsWith(key + ": ")) {
                        String value = line.split(": ", 2)[1].trim();
                        values.add(value);
                    }
                }
            }
            if (values.size() > 1) {
                return "Detainment: " + keyToMessage.get(key) + " mismatch.";
            }
        }
        return null; // всі документи співпадають
    }

    public boolean isExpired(String data) {
        String[] arrData = data.split("\\.");
        int yyyy = Integer.parseInt(arrData[0]);
        int mm = Integer.parseInt(arrData[1]);
        int dd = Integer.parseInt(arrData[2]);
        if (yyyy < 1982) return true;
        if (yyyy == 1982) {
            if (mm < 11) return true;
            if (mm == 11 && dd <= 22) return true;
        }
        return false;
    }

    public String getExpDateString(String exp, Map<String, String> person) {
        return Arrays.stream(person.get(exp).split("\n"))
                .filter(l -> l.startsWith("EXP: "))
                .map(l -> l.substring("EXP: ".length()).trim())
                .findFirst()
                .orElse(null);
    }

    public Set<String> getVaccines(String exp, Map<String, String> person) {
        return Arrays.stream(person.get(exp).split("\n"))
                .filter(l -> l.startsWith("VACCINES: "))
                .map(l -> l.substring("VACCINES: ".length()))
                .flatMap(v -> Arrays.stream(v.split(",\\s*")))
                .map(String::trim)
                .collect(Collectors.toSet());
    }


}
