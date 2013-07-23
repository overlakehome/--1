# color prompts, e.g. 32;40, or 30;46
export PS1='\[\e]2;\u@\h:\@:\w\a\e]1;$(basename $(dirname $(pwd)))/\W\a\e[32;40m\]\t:$(basename $(dirname $(pwd)))/\W>\[\e[0m\] '
export TERM='xterm-color'
export CLICOLOR=1
export LSCOLORS=ExFxCxDxBxegedabagacad

# homes and paths
export ANDROID_HOME=/workspace/android-sdk
export GROOVY_HOME=/usr/lib/groovy
export JAVA_HOME=/Library/Java/Home
export M3_HOME=/usr/local/Cellar/maven/3.0.4 # previously, /usr/share/maven
export SWIFTMQ_HOME=/workspace/swiftmq-7.5.3
export TOMCAT_HOME=/workspace/apache-tomcat-6.0.24
export PATH=$HOME/bin:/usr/local/bin:/usr/local/sbin:$PATH
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH

# app settings
export GREP_OPTIONS='--color=auto'
export CATALINA_OPTS='-Xmx1536m -XX:MaxPermSize=512m -Dlog.dir=/tmp/ -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,suspend=n,server=y'
export USE_CCACHE=1
ulimit -S -n 1024 # sets the file descriptor limit to 1024.

# aliases for git
source .git-aliases

# misc. aliases
alias grep='grep -n'
alias diff='colordiff'
alias gmail='openssl s_client -crlf -quiet -connect imap.gmail.com:993'
alias less='less -Mi'
alias l='ls -alv'
alias ll='ls -alv'
alias ls='ls -av'
alias m2eclipse='mvn eclipse:eclipse -DdownloadSources=true'
alias sdf='svn diff --diff-cmd=svn-diff'
alias vim='/Applications/MacVim.app/Contents/MacOS/Vim'
alias wget='wget --no-check-certificate'

# ssh-aliases
alias geio='ssh geio-7001.iad7'
alias hylee-x='ssh hylee.desktop'
alias hylee='ssh -X hylee.desktop'
alias ii='ssh i-interactive'

# cd aliases
alias ..='cd ..'
alias ...='cd ../..'
alias ....='cd ../../..'
alias .....='cd ../../../..'
alias gits='cd /workspace/gits'
alias henry='cd /workspace/gits/henry4j'
alias workspace='cd /workspace'
alias ws='cd /workspace'

eval "$(rbenv init -)"