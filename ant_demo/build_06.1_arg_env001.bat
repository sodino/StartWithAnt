@echo off 
echo REM DOSϵͳ���û��������ķ�ʽ: %%env.key%%
echo Unixϵͳ���û��������ķ�ʽ: $env.key
echo ʹ��Jad���߷�����.class�ļ������������������output_dir��

del %output_dir%\AntTest.jad

%jad_home%\jad  -d %output_dir% %class_file%

@echo on