//package db.migration;
//
//import org.flywaydb.core.api.migration.BaseJavaMigration;
//import org.flywaydb.core.api.migration.Context;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.regex.Pattern;
//
//public class V34__harden_user_identity extends BaseJavaMigration {
//
//    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.*");
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    @Override
//    public void migrate(Context context) throws Exception {
//        Connection connection = context.getConnection();
//
//        try (Statement statement = connection.createStatement()) {
//            statement.executeUpdate("ALTER TABLE user MODIFY email VARCHAR(200) NOT NULL");
//            statement.executeUpdate("ALTER TABLE user MODIFY role ENUM('ADMIN','USER','MANAGER','COLLECTOR') NOT NULL");
//        }
//
//        try (PreparedStatement select = connection.prepareStatement("SELECT id, password FROM user");
//             ResultSet resultSet = select.executeQuery();
//             PreparedStatement update = connection.prepareStatement("UPDATE user SET password = ? WHERE id = ?")) {
//
//            while (resultSet.next()) {
//                long id = resultSet.getLong("id");
//                String password = resultSet.getString("password");
//
//                if (password == null || password.isBlank()) {
//                    continue;
//                }
//
//                if (BCRYPT_PATTERN.matcher(password).matches()) {
//                    continue;
//                }
//
//                update.setString(1, passwordEncoder.encode(password));
//                update.setLong(2, id);
//                update.addBatch();
//            }
//
//            update.executeBatch();
//        }
//    }
//}
//
