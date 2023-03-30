# DukeWiki

A Wiki for markdown and text files saved in the file system. Perform [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) operations on markdown and text files (deployed on Java web-server like [Tomcat10](https://tomcat.apache.org/)) online using [WYSIWYG](https://en.wikipedia.org/wiki/WYSIWYG)  editor.

- A [WYSIWYG](https://en.wikipedia.org/wiki/WYSIWYG) markdown editor is provided by integrating [Toast UI Editor](https://ui.toast.com/tui-editor).  
- Markdown to HTML rendering is provided by [CommonMark-Java](https://github.com/commonmark/commonmark-java) 

# Getting started

1. Install [Tomcat10](https://tomcat.apache.org/download-10.cgi) (or higher)

2. Download DukeWiki.war file and copy it to [CATALINA_HOME](https://tomcat.apache.org/tomcat-10.0-doc/introduction.html#CATALINA_HOME_and_CATALINA_BASE) `/webapps`

3. Start Tomcat

4. Place all your existing files under [CATALINA_HOME](https://tomcat.apache.org/tomcat-10.0-doc/introduction.html#CATALINA_HOME_and_CATALINA_BASE) `/webapps/DukeWiki/home`

5. Access http://localhost:8080/DukeWiki/ (default host and port of Tomcat)

6. Login using 
   - `duke/cafebabe` (A user with `READER` and `ADMIN` roles)
   - `guest/cafebabe` (A user with only `READER` role)

   

# Change user name, password and roles

1. Encrypt your password using online tools like [JavaInUse-BCrypt](https://www.javainuse.com/onlineBcrypt) or [SpringBoot CLI](https://docs.spring.io/spring-boot/docs/current/reference/html/cli.html)  (number of rounds = 10)

2. Prefix `{bcrypt}` string to the encrypted password.

3. Replace following properties in `DukeWiki/WEB-INF/classes/application.properties` 

   | Property           | Detail                                                       |
   | ------------------ | ------------------------------------------------------------ |
   | dukewiki.usernames | Usernames separated by `|`<br />**Format:** `<user1>|<user2>|...|<userN>` |
   | dukewiki.passwords | Passwords separated by \|<br />**Format:** `<password1>|<password2>|...|<passwordN>` |
   | duke.roles         | A user can belong to READER role or both READER and ADMIN roles. Separate roles by `,`. Separate user-roles mapping using `|`<br />**Example:** `duke` belongs to roles READER and ADMIN. `guest` belongs to role READER<br />`dukewiki.usernames=duke|guest`<br />`dukewiki.roles=ADMIN,READER|READER`<br /> |

4. Restart server

# Technologies

| Tool / Technology                                            | Usage                                                        |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [SpringBoot MVC](https://spring.io/guides/gs/serving-web-content/) | A Java server side rendering web-framework (similar to JSP, JSF) to handle HTTP requests using MVC design pattern. |
| [Toast UI Editor](https://ui.toast.com/tui-editor)           | A WYSIWYG markdown editor                                    |
| [Toast UI Plugin to highlight code syntax](https://github.com/nhn/tui.editor/blob/master/plugins/code-syntax-highlight/README.md) | Syntax color highlighter for code block reandered via Toast UI Editor |
| [Prism JS](https://prismjs.com/)                             | Syntax color highlighter for code block in HTML              |
| [CommonMark-Java](https://github.com/commonmark/commonmark-java) | Render markdown as HTML to be displayed by browser           |
| [Lombok](https://projectlombok.org/)                         | A Java library to auto generate getters and setters.         |
| [Thymeleaf](https://www.thymeleaf.org/)                      | A template that works with HTML and SpringBoot-MVC to providing the view. <br />Provides features like fragment assembly enabling code reuse. |
| [Bootstrap](https://getbootstrap.com/)                       | Provides front-end library to create web components using HTML, JS and CSS |
| [JQuery](https://jquery.com/)                                | A fast rich JS library to ease HTML traversal and manipulation |
| [Google Material Icons](https://fonts.google.com/icons?icon.style=Filled&icon.set=Material+Icons) | A Google library for adding material icons to web page       |
| [Google Fonts](https://fonts.google.com/)                    | A Google library for adding fonts to web page (without requiring the font to be present on host machine) |


