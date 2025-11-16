import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class StudentManagementSystem {
    private static List<Student> students = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static int nextId = 1;

    public static void main(String[] args) {
        System.out.println("=== Student Management System ===");
        loadStudents();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { saveStudents(); } catch (Exception ignored) {}
        }));
        
        while (true) {
            System.out.println("\n1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Search Student");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    viewAllStudents();
                    break;
                case 3:
                    updateStudent();
                    break;
                case 4:
                    deleteStudent();
                    break;
                case 5:
                    searchStudent();
                    break;
                case 6:
                    saveStudents();
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void addStudent() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter course: ");
        String course = scanner.nextLine();
        System.out.print("Enter grade: ");
        double grade = scanner.nextDouble();
        
        Student student = new Student(nextId++, name, age, course, grade);
        students.add(student);
        System.out.println("Student added successfully!");
        saveStudents();
    }

    private static void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students found!");
            return;
        }
        System.out.println("\n=== All Students ===");
        for (Student student : students) {
            System.out.println(student);
        }
    }

    private static void updateStudent() {
        System.out.print("Enter student ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        
        System.out.print("Enter new name (current: " + student.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) student.setName(name);
        
        System.out.print("Enter new age (current: " + student.getAge() + "): ");
        String ageInput = scanner.nextLine();
        if (!ageInput.isEmpty()) student.setAge(Integer.parseInt(ageInput));
        
        System.out.print("Enter new course (current: " + student.getCourse() + "): ");
        String course = scanner.nextLine();
        if (!course.isEmpty()) student.setCourse(course);
        
        System.out.print("Enter new grade (current: " + student.getGrade() + "): ");
        String gradeInput = scanner.nextLine();
        if (!gradeInput.isEmpty()) student.setGrade(Double.parseDouble(gradeInput));
        
        System.out.println("Student updated successfully!");
        saveStudents();
    }

    private static void deleteStudent() {
        System.out.print("Enter student ID to delete: ");
        int id = scanner.nextInt();
        
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        
        students.remove(student);
        System.out.println("Student deleted successfully!");
        saveStudents();
    }

    private static void searchStudent() {
        System.out.println("Search by: 1. ID  2. Name  3. Course");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                System.out.print("Enter ID: ");
                int id = scanner.nextInt();
                Student student = findStudentById(id);
                if (student != null) {
                    System.out.println(student);
                } else {
                    System.out.println("Student not found!");
                }
                break;
            case 2:
                System.out.print("Enter name: ");
                String name = scanner.nextLine();
                List<Student> found = students.stream()
                    .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
                if (found.isEmpty()) {
                    System.out.println("No students found!");
                } else {
                    for (Student s : found) {
                        System.out.println(s);
                    }
                }
                break;
            case 3:
                System.out.print("Enter course: ");
                String course = scanner.nextLine();
                found = students.stream()
                    .filter(s -> s.getCourse().toLowerCase().contains(course.toLowerCase()))
                    .collect(Collectors.toList());
                if (found.isEmpty()) {
                    System.out.println("No students found!");
                } else {
                    for (Student s : found) {
                        System.out.println(s);
                    }
                }
                break;
        }
    }

    private static Student findStudentById(int id) {
        return students.stream()
            .filter(s -> s.getId() == id)
            .findFirst()
            .orElse(null);
    }

    private static void saveStudents() {
        File file = new File("students.csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Student s : students) {
                writer.printf("%d|%s|%d|%s|%f\n", s.getId(), escape(s.getName()), s.getAge(), escape(s.getCourse()), s.getGrade());
            }
        } catch (IOException e) {
            System.out.println("Error saving students: " + e.getMessage());
        }
    }

    private static void loadStudents() {
        File file = new File("students.csv");
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0]);
                    String name = unescape(parts[1]);
                    int age = Integer.parseInt(parts[2]);
                    String course = unescape(parts[3]);
                    double grade = Double.parseDouble(parts[4]);
                    students.add(new Student(id, name, age, course, grade));
                    if (id >= nextId) nextId = id + 1;
                }
            }
            System.out.println("Loaded students from file.");
        } catch (IOException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String s) {
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (char c : s.toCharArray()) {
            if (esc) {
                out.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}


