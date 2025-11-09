package com.github.ar4ik4ik.cloudstorage.mapper;

import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import com.github.ar4ik4ik.cloudstorage.utils.ResourceInfo;
import io.minio.GetObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {PathUtils.class, ResourceInfoResponseDto.ResourceType.class})
public interface ResourceMapper {

    // create empty directory & upload directories
    @Mapping(target = "path", expression = "java(PathUtils.getParentPath(directoryPath, false))")
    @Mapping(target = "name", expression = "java(PathUtils.extractNameFromPath(directoryPath))")
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "type", expression = "java(ResourceType.DIRECTORY.name())")
    ResourceInfoResponseDto toUploadDirectoryDto(String directoryPath);

    // get object info
    @Mapping(target = "path", expression = "java(PathUtils.getParentPath(directoryPath, true))")
    @Mapping(target = "name", expression = "java(PathUtils.extractNameFromPath(directoryPath))")
    @Mapping(target = "size", expression = "java(object.headers().byteCount())")
    @Mapping(target = "type", expression = "java(PathUtils.isFolder(directoryPath) ? ResourceType.DIRECTORY.name() : ResourceType.FILE.name())")
    ResourceInfoResponseDto toDto(String directoryPath, GetObjectResponse object);

    // search & full directory info
    @Mapping(target = "path", expression = "java(PathUtils.getParentPath(object.objectName(), true))")
    @Mapping(target = "name", expression = "java(PathUtils.extractNameFromPath(object.objectName()))")
    @Mapping(target = "size", expression = "java(object.size() > 0 ? object.size() : null)")
    @Mapping(target = "type", expression = "java(PathUtils.isFolder(object.objectName()) ? ResourceType.DIRECTORY.name() : ResourceType.FILE.name())")
    ResourceInfoResponseDto toDirectoryInfoDto(Item object);

    // only when uploading file
    @Mapping(target = "path", expression = "java(resourceInfo.getParentDirectoryPathForFile())")
    @Mapping(target = "name", expression = "java(resourceInfo.getFilename())")
    @Mapping(target = "size", expression = "java(resourceInfo.getMultipartFile().getSize())")
    @Mapping(target = "type", expression = "java(ResourceType.FILE.name())")
    ResourceInfoResponseDto toUploadFileDto(ResourceInfo resourceInfo);

    // only when moving (renaming) file or directory
    @Mapping(target = "path", expression = "java(to)")
    @Mapping(target = "name", expression = "java(PathUtils.extractNameFromPath(from))")
    @Mapping(target = "size", expression = "java(bytesCount)")
    @Mapping(target = "type", expression = "java(PathUtils.isFolder(from) ? ResourceType.DIRECTORY.name() : ResourceType.FILE.name())")
    ResourceInfoResponseDto toMoveResourceDto(String from, String to, long bytesCount);




}
