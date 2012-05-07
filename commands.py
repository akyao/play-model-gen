# Here you can create play commands that are specific to the module, and extend existing commands
import os, os.path
import getopt
import sys
import subprocess
from pprint import pprint

MODULE = 'modelgen'

# Commands that are specific to your module

COMMANDS = ['modelgen']

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    genDir = app.path + "\\" + app.conf.get("modelgen.genDir")
    rootPackage = app.conf.get("modelgen.rootPackage")
    host = app.conf.get("modelgen.host")
    scheme = app.conf.get("modelgen.scheme")
    user = app.conf.get("modelgen.user")
    password = app.conf.get("modelgen.pass")
    vmAbstract = app.conf.get("modelgen.vm.abstract")
    vmConcrete = app.conf.get("modelgen.vm.concrete")
    params = [genDir, rootPackage, host, scheme, user, password, vmAbstract, vmConcrete]


    print "~ Generating Model Classes from the database"
    print "~ "

    java_cmd = app.java_cmd([], None, "play.modules.modelGen.Generator", params)

    try:
        print "fuck"
        subprocess.call(java_cmd, env=os.environ)
    except OSError:
        print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
        sys.exit(-1)
    print

# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")


# This will be executed after any command (new, run...)
def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "new":
        pass

