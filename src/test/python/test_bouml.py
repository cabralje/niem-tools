import subprocess
import time
import asyncio
import async_timeout
import os
from pathlib import Path
import pytest

# configure niemtools
niemtools = "org.cabral.niemtools.NiemtoolsBouml"
niemtools_test = "org.cabral.niemtools.TestHarness"
niemtools_args = "export"

# configure paths
niemtools_path = "C:/Users/JamesCabral/git/niem-tools/niemtools-bouml/"
niemtools_classpath = "target/*"
bouml_exec = r"C:/Program Files (x86)/Bouml/bouml.exe"
bouml_project_path = "src/test/resources/crashdriver/"
bouml_project = "crashdriver"
bouml_lockfile = "2.lock"
bouml_plugout = f"java -cp {niemtools_path}{niemtools_classpath} {niemtools_test}"
bouml_portfile = "C:/tmp/boumlport.txt"
launch_bouml = f"{bouml_exec} {niemtools_path}{bouml_project_path}{bouml_project}.prj -exec {bouml_plugout} -exit"
launch_niemtools = f"java -cp {niemtools_path}{niemtools_classpath} {niemtools} {niemtools_args}"

processes = []

async def start_bouml(seconds):
    # delete lock file
    lockfile = f"{bouml_project_path}{bouml_lockfile}"
    if os.path.exists(lockfile):
        os.remove(lockfile)

    # delete port file
    if os.path.exists(bouml_portfile):
        os.remove(bouml_portfile)

    # launch bouml
    print("Launching BOUML: " + launch_bouml)
    processes.append(subprocess.Popen(launch_bouml))

    # wait for BOUML port
    while not os.path.exists(bouml_portfile):
        time.sleep(5)

async def start_niemtools(seconds):

    # launch niemtools
    print("Launching niemtools: " + launch_niemtools)
    processes.append(subprocess.Popen(launch_niemtools))

# start BOUML
async def main():
    try:
        async with async_timeout.timeout(180):
            #print("BOUML starting")
            await start_bouml(180)
            #print("BOUML ending")
    except asyncio.TimeoutError:
        print("Error - BOUML timed out")
        return 1

    # start niemtools
    try:
        async with async_timeout.timeout(60):
            #print("niemtools starting")
            await start_niemtools(60)
            #print("niemtools ending")
    except asyncio.TimeoutError:
        print("Error - niemtools timed out")
        return 2
            
    # wait for each process to finish
    for process in processes:
        process.wait() # Wait for all processes to finish
    
    print("All processes finished")
    return 0

def test_bouml():
    assert asyncio.run(main()) == 0

#def test_bouml():
if __name__ == "__main__":
    asyncio.run(main())

