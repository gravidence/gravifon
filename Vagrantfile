# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  config.vm.define "linuxJava" do |linuxJava|
    linuxJava.vm.box = "ubuntu/focal64"
    linuxJava.vm.provision "shell", inline: <<-SHELL
      sudo apt-get update -y
      sudo apt-get install -y default-jdk
      java -version
    SHELL
    linuxJava.vm.provision "shell", inline: <<-SHELL
      cd /vagrant
      ./gradlew clean packageUberJarForCurrentOS
      mkdir distrib
      \\cp ./gravifon/build/compose/jars/*.jar ./distrib
      exit
    SHELL
    linuxJava.vm.provider "virtualbox" do |vb|
        vb.memory = 2048
        vb.cpus = 2
    end
  end

  config.vm.define "windowsJava" do |windowsJava|
    windowsJava.vm.box = "gusztavvargadr/windows-10"
    windowsJava.vm.provision "shell", inline: <<-SHELL
      choco install Temurin -y --params="/ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome"
    SHELL
    windowsJava.vm.provision "shell", inline: <<-SHELL
      cd c:\\vagrant
      .\\gradlew.bat clean packageUberJarForCurrentOS
      mkdir distrib
      copy gravifon\\build\\compose\\jars\\*.jar distrib
      exit
    SHELL
    windowsJava.vm.provider "virtualbox" do |vb|
        vb.memory = 2048
        vb.cpus = 2
    end
  end

end
