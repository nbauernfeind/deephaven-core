package io.deephaven.auth.codegen;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.io.IOException;

public class GenerateContextualAuthWiring {
    public static void main(String[] args) throws IOException {
        final PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);
        final PluginProtos.CodeGeneratorResponse.Builder response = PluginProtos.CodeGeneratorResponse.newBuilder();

        // tell protoc that we support proto3's optional as synthetic oneof feature
        response.setSupportedFeatures(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL_VALUE);

        // for each service, generate the auth wiring
        for (final DescriptorProtos.FileDescriptorProto file : request.getProtoFileList()) {
            for (final DescriptorProtos.ServiceDescriptorProto service : file.getServiceList()) {
                generateForService(response, service);
            }
        }

        response.build().toByteString().writeTo(System.out);
    }

    private static void generateForService(
            final PluginProtos.CodeGeneratorResponse.Builder response,
            final DescriptorProtos.ServiceDescriptorProto service) {
        final String serviceName = service.getName() + "ContextualAuthWiring";
        System.err.println("Generating: " + serviceName);

        final StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        sb.append(" * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending\n");
        sb.append(" */\n");
        sb.append("/**\n");
        sb.append(" * ---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit GenerateContextualAuthWiring and regenerate\n");
        sb.append(" * ---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" */\n");
        sb.append("package io.deephaven.auth.codegen.impl;\n");
        sb.append("\n");
        sb.append("import com.google.rpc.Code;\n");
        sb.append("import io.deephaven.auth.AuthContext;\n");
        sb.append("import io.deephaven.engine.table.Table;\n");
        sb.append("import io.deephaven.proto.util.Exceptions;\n");
        sb.append("\n");
        sb.append("import java.util.List;\n");
        sb.append("\n");

        // define the interface
        sb.append("public interface ").append(serviceName).append(" {\n");
        visitAllMethods(service, sb, 1, false, () -> sb.append(";\n"));

        // create a default implementation that is permissive
        sb.append("\n");
        sb.append("    class AllowAll implements ").append(serviceName).append(" {\n");
        visitAllMethods(service, sb, 2, true, () -> {
            sb.append(" {}\n");
        });
        sb.append("    }\n");

        // create a default implementation that is restrictive
        sb.append("\n");
        sb.append("    class DenyAll implements ").append(serviceName).append(" {\n");
        visitAllMethods(service, sb, 2, true, () -> {
            sb.append(" {\n");
            sb.append("            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, \"Operation not allowed.\");\n");
            sb.append("        }\n");
        });
        sb.append("    }\n");

        sb.append("}\n");

        response.addFile(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName("io/deephaven/auth/codegen/impl/" + serviceName + ".java")
                .setContent(sb.toString())
                .build());
    }

    private static void visitAllMethods(
            DescriptorProtos.ServiceDescriptorProto service,
            StringBuilder sb,
            int numIndents,
            boolean isPublic,
            Runnable visitor) {
        final String indent = "    ".repeat(numIndents);
        final String prefix = isPublic ? "public " : "";
        for (final DescriptorProtos.MethodDescriptorProto method : service.getMethodList()) {
            if (method.getName().equals("Batch")) {
                // batch methods get broken up and will authorize each operation individually
                continue;
            }

            // strip off the leading '.', but otherwise we do not do anything fancy with the java package
            final String inputType = method.getInputType().substring(1);

            sb.append("\n");
            sb.append(indent).append(prefix).append("void checkPermission").append(method.getName()).append("(\n");
            sb.append(indent).append("    AuthContext authContext,\n");
            sb.append(indent).append("    ").append(inputType).append(" request,\n");
            sb.append(indent).append("    List<Table> tables)");

            visitor.run();
        }
    }
}
