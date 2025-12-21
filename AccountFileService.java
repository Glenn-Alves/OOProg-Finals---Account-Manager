package services;

import java.io.*;
import java.util.*;
import models.Account;
import enums.Country;

public class AccountFileService {

    public static List<Account> loadAccounts(String filePath) {
        List<Account> accounts = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) return accounts;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String name = null, birth = null, email = null, loc = null;
            int age = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Name:")) name = line.substring(5).trim();
                else if (line.startsWith("Age:")) age = Integer.parseInt(line.substring(4).trim());
                else if (line.startsWith("BirthYear:")) birth = line.substring(10).trim();
                else if (line.startsWith("Email:")) email = line.substring(6).trim();
                else if (line.startsWith("Country:")) loc = line.substring(9).trim();
                else if (line.startsWith("----------------")) {
                    if (name != null) {
                        Country country = Country.OTHER;
                        try { country = Country.valueOf(loc); } catch (Exception ignored) {}
                        accounts.add(new Account(name, age, birth, email, country));
                    }
                    name = null; birth = null; email = null; loc = null; age = 0;
                }
            }

            if (name != null) {
                Country country = Country.OTHER;
                try { country = Country.valueOf(loc); } catch (Exception ignored) {}
                accounts.add(new Account(name, age, birth, email, country));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public static void saveAccounts(String filePath, List<Account> accounts) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            int count = 1;
            for (Account a : accounts) {
                bw.write("Account " + count + ":\n");
                bw.write(a.toString() + "\n");
                bw.write("---------------------------\n");
                count++;
            }
        }
    }
}
