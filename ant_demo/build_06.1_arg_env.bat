@echo off 
echo REM DOS系统引用环境变量的方式: %%env.key%%
echo Unix系统引用环境变量的方式: $env.key
echo 使用Jad工具反编译.class文件并将反编译结果输出到output_dir中

del %output_dir%\AntTest.jad

%jad_home%\jad  -d %output_dir% %class_file%

@echo on