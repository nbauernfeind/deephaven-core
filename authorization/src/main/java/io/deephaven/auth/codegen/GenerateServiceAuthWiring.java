package io.deephaven.auth.codegen;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GenerateServiceAuthWiring {
    public static void main(String[] args) throws IOException {
        final PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);
        final PluginProtos.CodeGeneratorResponse.Builder response = PluginProtos.CodeGeneratorResponse.newBuilder();

        // tell protoc that we support proto3's optional as synthetic oneof feature
        response.setSupportedFeatures(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL_VALUE);

        // create a mapping from message type to java type name
        final Map<String, String> typeMap = generateTypeMap(request);

        // for each service, generate the auth wiring
        for (final DescriptorProtos.FileDescriptorProto file : request.getProtoFileList()) {
            for (final DescriptorProtos.ServiceDescriptorProto service : file.getServiceList()) {
                generateForService(response, service, typeMap);
            }
        }

        response.build().toByteString().writeTo(System.out);
    }

    private static Map<String, String> generateTypeMap(PluginProtos.CodeGeneratorRequest request) {
        final Map<String, String> typeMap = new HashMap<>();
        for (final DescriptorProtos.FileDescriptorProto file : request.getProtoFileList()) {
            String realPackage = null;
            if (file.hasOptions()) {
                realPackage = file.getOptions().getJavaPackage();
            }
            if (Strings.isNullOrEmpty(realPackage)) {
                realPackage = file.getPackage();
            }

            // Unsure of where this is specified in Flight.proto, but in addition to the package the messages are
            // put into a "Flight" namespace.
            if (realPackage.equals("org.apache.arrow.flight.impl")) {
                realPackage += ".Flight";
            }

            for (final DescriptorProtos.DescriptorProto message : file.getMessageTypeList()) {
                typeMap.put("." + file.getPackage() + "." + message.getName(), realPackage + "." + message.getName());
            }
        }
        return typeMap;
    }

    private static void generateForService(
            final PluginProtos.CodeGeneratorResponse.Builder response,
            final DescriptorProtos.ServiceDescriptorProto service,
            final Map<String, String> typeMap) {
        final String serviceName = service.getName() + "AuthWiring";
        System.err.println("Generating: " + serviceName);

        final StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        sb.append(" * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending\n");
        sb.append(" */\n");
        sb.append("/**\n");
        sb.append(" * ---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit GenerateServiceAuthWiring and regenerate\n");
        sb.append(" * ---------------------------------------------------------------------------------------------------------------------\n");
        sb.append(" */\n");
        sb.append("package io.deephaven.auth.codegen.impl;\n");
        sb.append("\n");
        sb.append("import com.google.rpc.Code;\n");
        sb.append("import io.deephaven.auth.AuthContext;\n");
        sb.append("import io.deephaven.auth.ServiceAuthWiring;\n");
        sb.append("import io.deephaven.proto.util.Exceptions;\n");
        sb.append("\n");

        // define the interface
        sb.append("public interface ").append(serviceName).append(" extends ServiceAuthWiring {\n");
        visitAllMethods(service, typeMap, sb, 1, false, () -> sb.append(";\n"));

        // create a default implementation that is permissive
        sb.append("\n");
        sb.append("    class AllowAll implements ").append(serviceName).append(" {\n");
        visitAllMethods(service, typeMap, sb, 2, true, () -> {
            sb.append(" {}\n");
        });
        sb.append("    }\n");

        // create a default implementation that is restrictive
        sb.append("\n");
        sb.append("    class DenyAll implements ").append(serviceName).append(" {\n");
        visitAllMethods(service, typeMap, sb, 2, true, () -> {
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
            Map<String, String> typeMap,
            StringBuilder sb,
            int numIndents,
            boolean isPublic,
            Runnable visitor) {
        final String indent = "    ".repeat(numIndents);
        final String prefix = isPublic ? "public " : "";
        for (final DescriptorProtos.MethodDescriptorProto method : service.getMethodList()) {
            final String inputType = method.getInputType();
            final String realType = typeMap.get(inputType);
            if (realType == null) {
                System.err.println("Could not find type for: " + inputType);
                System.exit(-1);
            }
            sb.append("\n");
            sb.append(indent).append(prefix).append("void checkPermission").append(method.getName()).append("(\n");

            if (method.getClientStreaming()) {
                // client streaming calls are initiated before the first message is received
                sb.append(indent).append("    AuthContext authContext)");
            } else {
                // otherwise, we'll pass the message into the check
                sb.append(indent).append("    AuthContext authContext,\n");
                sb.append(indent).append("    ").append(realType).append(" request)");
            }
            visitor.run();
        }
    }
}
