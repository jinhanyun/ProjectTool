package cc.oit.model;

import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 Example:

 @ Data
 @ Table(name = "T_BAS_USER")
 public class User extends Entity {

 @ Id
 private String id;

 @ OrderColumn
 private String name;

 @ OrderColumn(name = "DESC")
 private String userCode; // default column name : USER_CODE

 @ Column(name = "PASS")
 private String password;

 private String status;

 @ Column(name = "S_COLUMN") // default column name : SOME_COLUMN
 private String getSomeColumn() {
 return null;
 }

 @ Transient
 private String getSomeNoneColumn() {
 return null;
 }

 }
 *
 * Created by Chanedi
 */
@Data
public class Entity implements Serializable {

    private static final long serialVersionUID = 4212679023438415647L;

    @Id
    private String id;

    private Date createTime;

    private Date modifyTime;

    private String createUserCode;

    private String modifyUserCode;

}