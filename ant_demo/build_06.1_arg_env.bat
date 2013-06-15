@echo off 

del %output_dir%\AntTest.jad

%jad_home%\jad  -d %output_dir% %class_file%

@echo on