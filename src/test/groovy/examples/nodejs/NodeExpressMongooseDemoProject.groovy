package examples.nodejs
import bear.annotations.Configuration
import bear.annotations.Project
import bear.context.Fun
import bear.core.*
import bear.plugins.ConfigureServiceInput
import bear.plugins.DeploymentPlugin
import bear.plugins.db.DumpManagerPlugin
import bear.plugins.misc.ReleasesPlugin
import bear.plugins.mongo.MongoDbPlugin
import bear.plugins.nodejs.NodeJsPlugin
import bear.task.TaskCallable
import bear.task.TaskDef
import bear.vcs.GitCLIPlugin
import com.google.common.base.Function

import static bear.plugins.db.DumpManagerPlugin.DbType.mongo
import static bear.task.TaskResult.OK
/**
 * @author Andrey Chaschev chaschev@gmail.com
 */

@Project(shortName = "express-demo", name = "Node Express Mongoose Demo Deployment")
@Configuration(
    properties = ".bear/demos",
    stage = "u-3",
    vcs = "https://github.com/chaschev/node-express-mongoose-demo.git",
    branch = "master",
    useUI = true,
    user = "andrey",
    password = "1"
)
public class NodeExpressMongooseDemoProject extends BearProject<NodeExpressMongooseDemoProject> {
    // these are the plugins which are injected
    Bear bear;
    GitCLIPlugin git;
    NodeJsPlugin nodeJs;
    MongoDbPlugin mongoPlugin;
    DeploymentPlugin deployment;
    ReleasesPlugin releases;
    DumpManagerPlugin dumpManager;

    @Override
    protected GlobalContext configureMe(GlobalContextFactory factory) throws Exception
    {
        nodeJs.version.set("0.10.22");
        nodeJs.appCommand.set("server.js")
        nodeJs.projectPath.setEqualTo(bear.vcsBranchLocalPath);

        nodeJs.instancePorts.set("5000, 5001")
//        nodeJs.useWatchDog.set(false)

        dumpManager.dbType.set(mongo.toString());

        nodeJs.configureService.setDynamic({ SessionContext _ ->
            return { ConfigureServiceInput input ->
                input.service
                    .cd(_.var(releases.activatedRelease).get().path)
                    .exportVar("PORT", input.port + "");

                return null;
            } as Function;
        } as Fun);

        bear.stages.set(new Stages(global)
            .addQuick("one", "vm01")
            .addQuick("two", "vm01, vm02")
            .addQuick("three", "vm01, vm02, vm03")
            .addQuick("u-1", "vm04")
            .addQuick("u-2", "vm04, vm05")
            .addQuick("u-3", "vm04, vm05, vm06")
        );

        // this defines the deployment task
        defaultDeployment = deployment.newBuilder()
            .CheckoutFiles_2({_, task -> _.run(global.tasks.vcsUpdate); } as TaskCallable)
            .BuildAndCopy_3({_, task -> _.run(nodeJs.build, copyConfiguration); } as TaskCallable)
            .StopService_4({_, task -> _.run(nodeJs.stop); OK; } as TaskCallable)
            .StartService_6({_, task -> _.run(nodeJs.start, nodeJs.watchStart); } as TaskCallable)
            .endDeploy()
            .ifRollback()
            .beforeLinkSwitch({_, task -> _.run(nodeJs.stop); } as TaskCallable)
            .afterLinkSwitch({_, task -> _.run(nodeJs.start, nodeJs.watchStart); } as TaskCallable)
            .endRollback();

        return global;
    }

    def copyConfiguration = new TaskDef({ SessionContext _, task ->
        final String dir = _.var(releases.pendingRelease).path + "/config"

        _.sys.copy("config.example.js").to("config.js").inDir(dir).force().run().throwIfError();
        _.sys.copy("imager.example.js").to("imager.js").inDir(dir).force().run().throwIfError();

        OK
    } as TaskCallable);

    // main, can be run directly from an IDE
    static main(def args)
    {
//        new NodeExpressMongooseDemoProject().setup()

        // complete deployment:
        // checkout, build, stop, copy code to release, start
        // inspect startup logs, update upstart scripts
//        new NodeExpressMongooseDemoProject().deploy()

        //stop all 6 instances (3 VMs, 2 instances each)
        new NodeExpressMongooseDemoProject().deploy()

        //start all 6 instances
//        new NodeExpressMongooseDemoProject().start()
    }

}