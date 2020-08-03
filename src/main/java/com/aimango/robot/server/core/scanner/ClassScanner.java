package com.aimango.robot.server.core.scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    /**
     * 获取类路径下某个包下的所有类
     * @param packageName
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Set<Class> scan(String packageName) throws IOException, ClassNotFoundException {
        Set<Class> classes=new HashSet<>();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");
        while (resources.hasMoreElements()){
            URL url = resources.nextElement();
            URLClassLoader urlClassLoader=new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
            System.out.println(url.toString());

            String protocol = url.getProtocol();
            System.out.println("协议："+protocol);
            if ("jar".equals(protocol)){
                JarURLConnection jarURLConnection=(JarURLConnection) url.openConnection();
                JarFile jarFile = jarURLConnection.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()){
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    int index = name.indexOf(packagePath);
                    if (index!=-1&&name.endsWith(".class")){
                        String replace = name.substring(index, name.length() - 6).replace("/", ".");
                        Class clazz = urlClassLoader.loadClass(replace);
                        classes.add(clazz);
                    }
                }
            }else if("file".endsWith(protocol)){
                String path = url.getPath();
                String targetPath = path + "/" + packagePath;
                addClasses(targetPath,classes,packageName);
            }
        }
        return classes;
    }

    private static void addClasses(String path, Set<Class> classes, String packageName) throws ClassNotFoundException {
        File[] files = new File(path).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
            }
        });

        for (File file:files){
            String fileName = file.getName();
            if (file.isFile()){
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                String fullClassName = packageName + "." + className;
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(fullClassName);
                classes.add(clazz);
            }else {
                String subPackagePath = path + "/" + fileName;
                String subPackageName = packageName + "." + fileName;
                addClasses(subPackagePath,classes,subPackageName);
            }
        }
    }

    public static boolean isAnnotateWith(Class clazz, Class targetAnnotation){
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation:annotations){
            Class<? extends Annotation> aClass = annotation.annotationType();
            if (aClass.equals(targetAnnotation)){
                return true;
            }
            if (aClass.isAnnotationPresent(targetAnnotation)){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Set<Class> scan = ClassScanner.scan("com.aimango.server.component");
        for (Class clazz:scan){
            System.out.println(clazz.getName());
        }
    }
}
