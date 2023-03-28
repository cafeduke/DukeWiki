package com.github.cafeduke.dukewiki.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.SpringVersion;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.ServletContext;
import lombok.Getter;
import lombok.Setter;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Controller
public class DirectoryListingController
{
    public static final String URI_HOME = "/home";
    
    public static final String LINESEP = System.getProperty("line.separator");
    
    @Autowired
    ServletContext servletContext;
    
    public static void main (String arg[]) throws IOException
    {
        String text = "\n\n\nHello how are you\n Ok then\n\n##comment here\nThis is #inline\nBye then\n";
        System.out.println("-----------------------------------------");
        System.out.println(text);
        System.out.println("-----------------------------------------");
        
        StringBuilder buffer = new StringBuilder();
        BufferedReader in = new BufferedReader(new StringReader(text));
        String line = null;        
        String styleBegin = "<pre id=\"comment\">";
        String plainBegin = "<pre>";
        String end   = "</pre>";
        while ((line = in.readLine()) != null)
        {
            int index = line.indexOf('#');
            if (index == -1)
               buffer.append(plainBegin).append(line).append(end); 
            else if (index == 0)
               buffer.append(styleBegin).append(line).append(end); 
            else
               buffer.append(plainBegin).append(line.substring(0, index)).append(end)
                     .append(styleBegin).append(line.substring(index)).append(end);
            buffer.append(LINESEP);
        }
        in.close();        
        
        System.out.println(buffer.toString());
    }
    
    @GetMapping("/work.do")
    @ResponseBody
    public String work (ModelMap model, @RequestParam("path") String path)
    {
        PathContext context = this.new PathContext(path);
        StringBuilder builder = new StringBuilder();
        
        builder.append("SpringVersion=").append(SpringVersion.getVersion()).append("<br/>")
            .append("ContextRoot=").append(servletContext.getContextPath()).append("<br/>")
            .append("Path=").append(path).append("<br/>")
            .append("RequestURI=").append(context.requestURI).append("<br/>")
            .append("FileTarget=").append(context.fileTarget).append("<br/>")
            .append("RealPath=").append(servletContext.getRealPath("/home" + path)).append("<br/>");
        return builder.toString();
    }
 
    
    @GetMapping("/list.do")
    public String doList(ModelMap model, @RequestParam(defaultValue = URI_HOME) String path)
    {
        System.out.println("[list.do] In here");
        String viewName = "list";
        PathContext context = this.new PathContext(path);
        
        if(!context.fileTarget.isDirectory())
            return viewName;
        
        List<FileInfo> listDir = FileInfo.getInfo(context.fileTarget.listFiles(f -> f.isDirectory()));
        
        List<FileInfo> listMdFile = new ArrayList<>();
        List<FileInfo> listTxtFile = new ArrayList<>();
        List<FileInfo> listOtherFile = new ArrayList<>();
        for (File file : context.fileTarget.listFiles(f -> f.isFile()))
        {
            List<FileInfo> list = file.getName().endsWith(".md") ? listMdFile : (file.getName().endsWith(".txt") ? listTxtFile : listOtherFile);
            list.add(new FileInfo(file));
        }
        
        Collections.sort(listDir);
        Collections.sort(listMdFile);
        Collections.sort(listOtherFile);
        
        Map<String,String> mapCrumbURI = getBreadcrumbs(context.requestURI);
        
        model.addAttribute("dirName", context.fileTarget.getName());
        model.addAttribute("dirPath", context.requestURI);
        model.addAttribute("mapCrumbURI", mapCrumbURI);
        model.addAttribute("childDirs", listDir);
        model.addAttribute("childMdFiles", listMdFile);
        model.addAttribute("childTxtFiles", listTxtFile);
        model.addAttribute("childFiles", listOtherFile);
        
        return viewName;
    }
    
    @GetMapping("/renderMd.do")
    public String doRenderMd (ModelMap model, @RequestParam("path") String path) throws IOException
    {
        String viewName = "renderMd";
        PathContext context = this.new PathContext(path);
        String contentMd = Files.readString (context.fileTarget.toPath());
    
        /* CommonMark: Convert markdown to html */
        
        // Extension to CommonMark
        List<Extension> extensions = Arrays.asList(TablesExtension.create());        
        Parser parser = Parser.builder().extensions(extensions).build();        
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        Node document = parser.parse(contentMd);
        String contentHtml = renderer.render(document); 
        // System.out.println("[doRender] Html=" + contentHtml);        
        
        Map<String,String> mapCrumbURI = getBreadcrumbs(context.requestURI);
        model.addAttribute("content", contentHtml);
        model.addAttribute("mapCrumbURI", mapCrumbURI);
        model.addAttribute("path", context.parentURI);
        model.addAttribute("fileName", context.fileTarget.getName());
        
        return viewName;
    }
    
    @GetMapping("/renderTxt.do")
    public String doRenderTxt (ModelMap model, @RequestParam("path") String path) throws IOException
    {
        String viewName = "renderTxt";
        
        PathContext context = this.new PathContext(path);
        String contentTxt = Files.readString (context.fileTarget.toPath());

        Map<String,String> mapCrumbURI = getBreadcrumbs(context.requestURI);
        model.addAttribute("content", convertTextToHtml(contentTxt));
        model.addAttribute("mapCrumbURI", mapCrumbURI);
        model.addAttribute("path", Path.of(context.requestURI).getParent().toString());
        model.addAttribute("fileName", context.fileTarget.getName());
        
        return viewName;
    }
    
    public String convertTextToHtml (String text) throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(LINESEP).append("<pre>").append(LINESEP);
        
        BufferedReader in = new BufferedReader(new StringReader(text));
        String line = null;        
        String styleBegin = "<span id=\"comment\">";
        String end   = "</span>";
        while ((line = in.readLine()) != null)
        {
            int index = line.indexOf('#');
            if (index == -1)
               buffer.append(line); 
            else if (index == 0)
               buffer.append(styleBegin).append(line).append(end); 
            else
               buffer.append(line.substring(0, index))
                     .append(styleBegin).append(line.substring(index)).append(end);
            buffer.append(LINESEP);
        }
        in.close();        
        buffer.append(LINESEP).append("</pre>").append(LINESEP);
        return buffer.toString();
    }
    

    @PostMapping("/editMd.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String doEditMd(ModelMap model, @RequestParam String path) throws IOException
    {
        String viewName = "editMd";
        PathContext context = this.new PathContext(path);
        
        model.addAttribute("fileName", context.fileTarget.getName());
        model.addAttribute("path", context.requestURI);
        model.addAttribute("content", Files.readString (context.fileTarget.toPath()));
        
        return viewName;
    }
    
    @PostMapping("/editTxt.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String doEditTxt(ModelMap model, @RequestParam String path) throws IOException
    {
        String viewName = "editTxt";
        PathContext context = this.new PathContext(path);
        
        model.addAttribute("fileName", context.fileTarget.getName());
        model.addAttribute("path", context.requestURI);
        model.addAttribute("content", Files.readString (context.fileTarget.toPath()));
        return viewName;
    }    

    @PostMapping("/save.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ModelAndView doSave(@RequestParam String path, @RequestParam String content) throws IOException
    {
        PathContext context = this.new PathContext(path);
        
        // fileTarget shall point to the file to be overwritten/created.
        if (context.fileTarget.isDirectory())
          throw new IllegalStateException("File to be saved is a directory. File=" + context.fileTarget.getAbsolutePath());
    
        // Save file
        Files.writeString(context.fileTarget.toPath(), content);  
        return new ModelAndView("redirect:" + context.parentURI);
    }

    @PostMapping("/cancel.do")
    public ModelAndView doCancel (@RequestParam String path)
    {
        PathContext context = this.new PathContext(path);
        return new ModelAndView("redirect:" + context.parentURI);
    }

    @PostMapping("/new-file.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String doNewFile (ModelMap model, @RequestParam String path, @RequestParam String name) throws IOException
    {
        PathContext context = this.new PathContext(path);
        File fileNew = new File(context.fileTarget, name);
        
        StringBuilder builder = new StringBuilder ();
        builder.append("# Title ").append("\n")
        .append("# Introduction ").append("\n")
        .append("Introduction goes here ").append("\n")        
        .append("\n")
        .append("# Detail ").append("\n")
        .append("Details go here ").append("\n")
        .append("\n");
        Files.writeString(fileNew.toPath(), builder.toString());  
        
        return doEditMd(model, Path.of(path, name).toString());
    }    
    
    @PostMapping("/new-folder.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ModelAndView doNewFolder (@RequestParam String path, @RequestParam String name) throws IOException
    {
        PathContext context = this.new PathContext(path);
        File dirTarget = context.fileTarget;        
        File dirNewTarget = new File(dirTarget, name);
        
        if (dirNewTarget.exists())
            throw new IllegalStateException ("Cannnot create new folder as the path already exists. Path=" + dirNewTarget.getAbsolutePath());
        
        if (!dirNewTarget.mkdirs())
            throw new IllegalStateException("Folder creation failed");
        
        return new ModelAndView("redirect:" + context.requestURI);
    }
    
    
    @PostMapping("/rename.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ModelAndView doRename (@RequestParam String path, @RequestParam String name) throws IOException
    {
        PathContext context = this.new PathContext(path);
        File fileCurrent = context.fileTarget;
        File fileNew     = new File (fileCurrent.getParentFile(), name);
        
        if (fileNew.equals(fileCurrent))
            throw new IllegalStateException("Current and new file are same. Current=" + fileCurrent.getAbsolutePath() + " New=" + fileNew.getAbsolutePath());
        fileCurrent.renameTo(fileNew);
        
        return new ModelAndView("redirect:" + context.parentURI);
    }    
    
    
    @PostMapping("/delete.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ModelAndView doDelete (@RequestParam String path) throws IOException
    {
        PathContext context = this.new PathContext(path);
        FileUtils.forceDelete(context.fileTarget);
        return new ModelAndView("redirect:" + context.parentURI);
    }
    
    /**
     * A mapping for '/login' is required to customize the HTML form based login.
     * 
     * @return The view name to be rendered.
     */
    @GetMapping("/login")
    public String doLogin() 
    {
        return "login";
    }    
    
    
    @GetMapping("/download.do")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")    
    @ResponseBody
    public ResponseEntity<Resource> doDownload(@RequestParam String path) throws IOException 
    {
        PathContext context = this.new PathContext(path);
        FileSystemResource resource = new FileSystemResource(context.fileTarget);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    } 

    /**
     * Return a map of crumb name to the URI (that takes to to the corresponding dir listing)
     *  
     * @param requestURI relative URI to current directory
     * @return a map of crumb name to URI
     */
    private Map<String,String> getBreadcrumbs (String requestURI)
    {
        Map<String,String> mapCrumbURI = new LinkedHashMap<>();
        String prefix = "";
        for (String crumb : requestURI.replaceFirst("/", "").split("/"))
        {
            prefix = prefix + "/" + crumb;
            mapCrumbURI.put(crumb, prefix);
        }     
        return mapCrumbURI;
    }
    
    public static class FileInfo implements Comparable<FileInfo>
    {
        public static final SimpleDateFormat formatDate = new SimpleDateFormat("EEE, dd-MMM-yyyy");
        
        public static final SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm:ss a", Locale.US);

        @Getter
        @Setter    
        private String name, mdate, mtime, size;
        
        public FileInfo (File file)
        {
            this.name = file.getName();
            this.mdate = formatDate.format(new Date(file.lastModified()));
            this.mtime = formatTime.format(new Date(file.lastModified()));
            this.size = null;
            
            long len = file.length();            
            long bucketSize[] = new long [] {1000000000L, 1000000L, 1000L}; 
            String bucketName[] = new String[] {"GB", "MB", "KB"};            
            for (int i = 0; i < bucketSize.length; ++i)
                if (len >= bucketSize[i])
                {
                   size = String.format("%.1f %s", ((double)len/bucketSize[i]) ,bucketName[i]);
                   break;
                }
            if (this.size == null)
                this.size = String.valueOf(file.length()) + " B";
        }
        
        public static List<FileInfo> getInfo (File... file)
        {
           return new ArrayList<FileInfo> (Stream.of(file)
             .map(f -> new FileInfo(f))
             .toList());
        }

        @Override
        public int compareTo(FileInfo info)
        {
            return this.name.compareTo(info.name);
        }
    }
    
    private class PathContext
    {
        private final String requestURI;
        
        private final String parentURI;
        
        private final File fileTarget;
        
        private PathContext (String path)
        {
            requestURI = Path.of(path).toString();              
            parentURI = Path.of(requestURI).getParent().toString();
            fileTarget = new File(servletContext.getRealPath(requestURI));
        }
    }
}
