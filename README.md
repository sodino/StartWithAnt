StartWithAnt
============

Ant入门教程

GitHub for windows问题解决：
1. 命令行下：
	问题描述：
	git clone https://github.com/sodino/git_project.git
	error: Connection time-out while accessing https://github.com/sodino/git_project.git/info/refs?service=git-upload-pack
	fatal:HTTP REQUEST FAILED
	
	解决：
	1.1 先执行 export http_proxy=http://proxy.admin.com:port
	1.2 再执行 git clone https://github.com/sodino/git_project.git 即可
	
2. 命令行可以了，但客户端依然无法操作。
	进入GitHub命令行工具，进入git_project目录，执行以下命令：
	git config http.proxy http://proxy.admin.com:port
	即可。正常啦。